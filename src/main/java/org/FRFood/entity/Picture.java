package org.FRFood.entity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

public class Picture {
    private int id;
    private final byte[] imageData;
    private final LocalDateTime dateAdded;

    Picture(byte[] imageData , LocalDateTime uploadTime){
        this.imageData = imageData;
        this.dateAdded = uploadTime;
    }

    public BufferedImage getImage() throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(this.imageData);
        return ImageIO.read(bis);
    }

    public LocalDateTime getdate() {
        return  this.dateAdded;
    }

    public int getId() {
        return id;
    }
}
