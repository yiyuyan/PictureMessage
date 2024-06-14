package cn.ksmcbrigade.pm;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static cn.ksmcbrigade.pm.PictureMessage.SEND_NETWORK;

public class Utils {
    public static void runInThread(Runnable runnable){
        new Thread(()-> {
            try {
                runnable.run();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    public static void sendPacketToClient(byte[] str, ServerPlayer player) {
        SEND_NETWORK.sendTo(new ByteMessage(str),player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendPacketToServer(byte[] str) {
        SEND_NETWORK.sendToServer(new ByteMessage(str));
    }

    public static byte[] getBytes(byte[] bytes, int length) {
        byte[] bytes1 = new byte[length];
        System.arraycopy(bytes, 0, bytes1, 0, length);
        return bytes1;
    }

    public static byte[] removeBytes(byte[] bytes, int length) {
        byte[] newBytes = new byte[bytes.length - length];
        System.arraycopy(bytes, length, newBytes, 0, bytes.length - length);
        return newBytes;
    }

    public static void appendFile(File file, byte[] bytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            fos.write(bytes);
        }
    }

    public static boolean isImage(byte[] bytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            BufferedImage image = ImageIO.read(bais);
            if (image != null) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isWebp(byte[] bytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            ImageInputStream iis = ImageIO.createImageInputStream(bais);
            if (iis != null) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isImageOrWebp(byte[] bytes) {
        if (isImage(bytes)) {
            return true;
        }
        if (isWebp(bytes)) {
            return true;
        }
        return false;
    }

    public static class ByteMessage {
        public final byte[] message;

        public ByteMessage(byte[] message) {
            this.message = message;
        }

        public byte[] getMessage() {
            return message;
        }

        public static void encode(ByteMessage msg, FriendlyByteBuf buf) {
            buf.writeBytes(msg.message);
        }

        public static ByteMessage decode(FriendlyByteBuf buf) {return new ByteMessage(((bytesPacketFix)buf).readLong(buf.readableBytes()));
        }
    }

    public static class Message {
        public final String message;

        public Message(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public static void encode(Message msg, FriendlyByteBuf buf) {
            buf.writeUtf(msg.message);
        }

        public static Message decode(FriendlyByteBuf buf) {return new Message(buf.readUtf());
        }
    }

    public interface networkPlayer{

        void set(boolean sendSet);

        boolean get();

        void set2(boolean retSet);

        void set3(boolean needSet);

        void set4(boolean sentSet);

        boolean get2();

        boolean get3();

        boolean get4();
    }

    public interface bytesPacketFix{
        byte[] readLong(int p_130102_);
    }
}
