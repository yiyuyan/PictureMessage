package cn.ksmcbrigade.pm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

@Mod(PictureMessage.MODID)
@Mod.EventBusSubscriber
public class PictureMessage {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "pm";

    public static final SimpleChannel SEND_NETWORK = NetworkRegistry.newSimpleChannel(new ResourceLocation("pm","send"),()->"345",(s) -> true, (s) -> true);
    public static final SimpleChannel GET_NETWORK = NetworkRegistry.newSimpleChannel(new ResourceLocation("pm","get"),()->"340",(s) -> true, (s) -> true);

    public static File file = new File("config/pm-config.json");
    public static boolean sc = true;
    public static byte[] start = "NXT".getBytes();
    public static byte[] end = "END".getBytes();

    public PictureMessage() throws IOException {
        MinecraftForge.EVENT_BUS.register(this);
        GET_NETWORK.registerMessage(0, Utils.Message.class, Utils.Message::encode, Utils.Message::decode,((message, contextSupplier) -> {
            ((Utils.networkPlayer)contextSupplier.get().getSender()).set4(true);
            contextSupplier.get().setPacketHandled(true);
        }));
        System.out.println("Server packet done.");
        registerMessages();
        System.out.println("Client packet done.");
        System.out.println("start: "+Arrays.toString(start));
        System.out.println("end: "+Arrays.toString(end));

        if(!file.exists()){
            JsonObject data = new JsonObject();
            data.addProperty("client-screen",sc);
            Files.writeString(file.toPath(),data.toString());
        }

        sc = JsonParser.parseString(Files.readString(file.toPath())).getAsJsonObject().get("client-screen").getAsBoolean();

        System.out.println("Done!!!!!!!!!!!!!!!!!!!!!");
    }

    @SubscribeEvent
    public void command(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("sendPicture").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("file", StringArgumentType.string()).executes(context -> {
            ServerPlayer player = EntityArgument.getPlayer(context,"player");
            Utils.networkPlayer networkPlayer = (Utils.networkPlayer) player;
            File file = new File(StringArgumentType.getString(context,"file"));
            System.out.println(!file.exists());
            if(!file.exists()){
                return 1;
            }
            Utils.runInThread(()->{
                networkPlayer.set(false);
                networkPlayer.set2(false);
                networkPlayer.set3(true);

                Component component = Component.translatable("gui.pm.title").withStyle(ChatFormatting.YELLOW).append(Component.literal(context.getSource().getTextName()).withStyle(ChatFormatting.GOLD)).append(") ").append(Component.translatable("gui.pm.yes").withStyle(ChatFormatting.BLUE).withStyle((s)-> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/pm-yes")))).append(" ").append(Component.translatable("gui.pm.no").withStyle(ChatFormatting.RED).withStyle((s2) -> s2.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/pm-no"))));
                player.sendSystemMessage(component);

                long startTime = System.currentTimeMillis();

                while (!networkPlayer.get() || !networkPlayer.get2()){
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime >= 2 * 60 * 1000) {
                        System.out.println("Timeout");
                        break;
                    }
                    if(networkPlayer.get()){
                        break;
                    }
                    if(networkPlayer.get2()){
                        break;
                    }
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(networkPlayer.get2()){
                    context.getSource().sendSystemMessage(Component.translatable("command.pm.ret").withStyle(ChatFormatting.YELLOW));
                    return;
                }
                if(networkPlayer.get()){
                    System.out.println("Sending...");
                    context.getSource().sendSystemMessage(Component.translatable("command.pm.send").withStyle(ChatFormatting.YELLOW));
                    networkPlayer.set4(false);

                    Utils.runInThread(() -> {
                        try {
                            Utils.sendPacketToClient(Files.readAllBytes(file.toPath()),player);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    while (!networkPlayer.get4()){
                        if(networkPlayer.get4()){
                            break;
                        }
                        try {
                            Thread.sleep(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    context.getSource().sendSystemMessage(Component.translatable("command.pm.ret_server").withStyle(ChatFormatting.GOLD));
                    networkPlayer.set4(false);

                    return;
                }
                if(!networkPlayer.get() && !networkPlayer.get2()){
                    context.getSource().sendSystemMessage(Component.translatable("command.pm.timeout").withStyle(ChatFormatting.RED));
                }
                networkPlayer.set(false);
                networkPlayer.set2(false);
                networkPlayer.set3(false);
            });
            return 0;
        }))));

        event.getDispatcher().register(Commands.literal("pm-yes").executes(context -> {
            if(context.getSource().getPlayer() instanceof Utils.networkPlayer player){
                if(player.get3()){
                    player.set(true);
                }
            }
            return 0;
        }));

        event.getDispatcher().register(Commands.literal("pm-no").executes(context -> {
            if(context.getSource().getPlayer() instanceof Utils.networkPlayer player){
                if(player.get3()){
                    player.set2(true);
                }
            }
            return 0;
        }));
    }

    public static void registerMessages() {

        SEND_NETWORK.registerMessage(0, Utils.ByteMessage.class, Utils.ByteMessage::encode, Utils.ByteMessage::decode,((message, contextSupplier) -> {
            contextSupplier.get().enqueueWork(()-> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    handlePacket(message,contextSupplier);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            contextSupplier.get().setPacketHandled(true);
        }));
    }

    public static void handlePacket(Utils.ByteMessage msg, Supplier<NetworkEvent.Context> ctx) throws IOException {
        Utils.runInThread(() -> {
            String name = UUID.randomUUID().toString().replace("-","");
            File file = new File(name+".png");
            if(!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Files.write(file.toPath(),msg.message);
            } catch (IOException e) {
                e.printStackTrace();
            }
            boolean win = System.getProperty("os.name").toLowerCase().contains("windows");
            File cmd = new File("line."+(win?"cmd":"sh"));
            try {
                Files.writeString(cmd.toPath(),(win?"del /f /q ":"rm ")+name+".png");
            } catch (IOException e) {
                e.printStackTrace();
            }
            GET_NETWORK.sendToServer(new Utils.Message("sent"));
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("file.pm.ask").withStyle(ChatFormatting.YELLOW).append(Component.translatable("file.pm.open").withStyle(ChatFormatting.BLUE).withStyle((s) -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE,file.getPath())))).append(" ").append(Component.translatable("file.pm.del").withStyle(ChatFormatting.RED).withStyle((s) -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE,cmd.getPath())))));
        });
    }
}
