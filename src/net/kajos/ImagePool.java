package net.kajos;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class ImagePool {
    Semaphore sem = new Semaphore(1);
    LinkedList<ImageWrapper> pool = new LinkedList<>();

    int width, height, type;
    public ImagePool(int width, int height, int type) {
        reset(width, height, type);
    }

    public void reset(int width, int height, int type) {
        pool.clear();
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public ImageWrapper get() {
        try {
            sem.acquire();
            ImageWrapper img;
            if (pool.size() == 0) {
                img = new ImageWrapper(width, height, type);
            } else {
                img = pool.remove();
            }
            sem.release();
            return img;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void put(ImageWrapper img) {
        try {
            sem.acquire();
            pool.add(img);
            sem.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
