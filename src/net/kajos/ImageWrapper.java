package net.kajos;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageWrapper {
    public BufferedImage image;
    public Graphics2D graphics;
    public ByteArrayOutputStream output;
    public ImageWriter writer;
    public ImageWriteParam imgParam;
    public int width, height, type;
    public int[] pixels;

    public ImageWrapper(BufferedImage image) {
        this.image = image;
        width = image.getWidth();
        height = image.getHeight();
        type = image.getType();

        graphics = image.createGraphics();
        output = new ByteArrayOutputStream();

        writer = ImageIO.getImageWritersByFormatName("jpg").next();
        imgParam = writer.getDefaultWriteParam();
        imgParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        imgParam.setCompressionQuality(1f);
        imgParam.setProgressiveMode(ImageWriteParam.MODE_DISABLED);

        try {
            ImageOutputStream outputStream = ImageIO.createImageOutputStream(output);
            writer.setOutput(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    public ImageWrapper(int width, int height, int type) {
        this(new BufferedImage(width, height, type));
    }

    private static final byte[] EMPTY = new byte[]{0};
    public byte[] getCompressedBytes(int type, int framestamp, float quality, String format) {
        imgParam.setCompressionQuality(quality);
        output.reset();
        output.write(type);
        switch (format) {
            case Constants.JPEG: output.write(0);
                break;
            case Constants.GIF: output.write(1);
                break;
            case Constants.PNG: output.write(2);
                break;
            default: return EMPTY;
        }
        output.write(framestamp & 0xff);
        framestamp >>= 8;
        output.write(framestamp & 0xff);
        framestamp >>= 8;
        output.write(framestamp & 0xff);
        framestamp >>= 8;
        output.write(framestamp & 0xff);
        try {
            if (format.equals("jpeg")) {
                IIOImage outputImage = new IIOImage(image, null, null);
                writer.write(null, outputImage,
                        imgParam);
            } else {
                    ImageIO.write(image, format, output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toByteArray();
    }
}
