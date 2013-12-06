
package com.wz.gif.encoder;

import com.wz.gif.util.Log;
import com.wz.gif.util.TimeUtil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class AnimatedGifEncoder {
    protected boolean closeStream;

    protected int colorDepth;

    protected byte[] colorTab;

    protected int delay = 0;

    protected int dispose;

    protected boolean firstFrame;

    protected int height;

    protected Bitmap image;

    protected byte[] indexedPixels;

    protected OutputStream out;

    protected int palSize;

    protected byte[] pixels;

    protected int repeat = -1;

    protected int sample;

    protected boolean sizeSet;

    protected boolean started;

    protected int transIndex;

    protected int transparent = 0;

    protected boolean[] usedEntry;

    protected int width;

    public AnimatedGifEncoder() {
        boolean[] arrayOfBoolean = new boolean[256];
        this.usedEntry = arrayOfBoolean;
        this.palSize = 7;
        this.dispose = -1;
        this.closeStream = false;
        this.firstFrame = true;
        this.sizeSet = false;
        this.sample = 10;
    }

    public boolean addFrame(Bitmap paramBitmap, int index) {
        boolean ok = true;
        if (paramBitmap == null || !started) {
            return false;
        }

        try {
//            Log.d("添加第 ：" + index + " 张图片 ：" + paramBitmap);
            if (!sizeSet) {
                int i = paramBitmap.getWidth();
                int l = paramBitmap.getHeight();
                setSize(i, l);
            }
            this.image = paramBitmap;
                
            //TODO:检测函数运行时间
            long time01 = System.currentTimeMillis();
            getImagePixels();
            TimeUtil.logTime(time01, System.currentTimeMillis(), "getImagePixels");
            long time02 = System.currentTimeMillis();
            analyzePixels();
            TimeUtil.logTime(time02, System.currentTimeMillis(), "analyzePixels");
            if (firstFrame) {
                writeLSD();
                writePalette();
                if (repeat >= 0)
                    writeNetscapeExt();
            }
            writeGraphicCtrlExt();
            writeImageDesc();

            if (!firstFrame)
                writePalette();
            writePixels();
            this.firstFrame = false;
            Log.d("ok?");
        } catch (IOException localIOException1) {
            ok = false;

        }
        return ok;

    }

    /**
     *这个函数耗时很严重
     */
    protected void analyzePixels() {
        int len = this.pixels.length;
        int nPix = len / 3;
        byte[] arrayOfByte1 = new byte[nPix];
        this.indexedPixels = arrayOfByte1;
        byte[] arrayOfByte2 = this.pixels;
        int k = this.sample;
        NeuQuant nq = new NeuQuant(arrayOfByte2, len, k);
        //TODO:计算时间
        long time01 = System.currentTimeMillis();
        this.colorTab = nq.process();
        TimeUtil.logTime(time01, System.currentTimeMillis(), "NeuQuant.process");
        int l = 0;
        int i1 = this.colorTab.length;
        if (l >= i1) {
            l = 0;
        }

        for (int i = 0; i < colorTab.length; i += 3) {
            byte temp = colorTab[i];
            colorTab[i] = colorTab[i + 2];
            colorTab[i + 2] = temp;
            usedEntry[i / 3] = false;
        }
        int k1 = 0;
        for (int i = 0; i < nPix; i++) {
            int index = nq.map(pixels[k1++] & 0xff, pixels[k1++] & 0xff, pixels[k1++] & 0xff);
            usedEntry[index] = true;
            indexedPixels[i] = (byte) index;
        }
        pixels = null;
        colorDepth = 8;
        palSize = 7;
        if (transparent != 0) {
            transIndex = findClosest(transparent);
        }

    }

    protected int findClosest(int paramInt) {

        if (colorTab == null) {
            return -1;
        }
        int r = Color.red(paramInt);
        int g = Color.green(paramInt);
        int b = Color.blue(paramInt);
        int minpos = 0;
        int dmin = 256 * 256 * 256;
        int len = colorTab.length;

        for (int i = 0; i < len;) {
            int dr = r - (colorTab[i++] & 0xff);
            int dg = g - (colorTab[i++] & 0xff);
            int db = b - (colorTab[i] & 0xff);
            int d = dr * dr + dg * dg + db * db;
            int index = i / 3;
            if (usedEntry[index] && (d < dmin)) {
                dmin = d;
                minpos = index;
            }
            i++;
        }
        return minpos;
    }

    public boolean finish() {
        if (!started)
            return false;
        boolean ok = true;
        started = false;
        try {
            out.write(0x3b); // gif trailer
            out.flush();//将缓冲区清除，将数据写入到基础设备
            if (closeStream) {
                out.close();
            }
        } catch (IOException e) {
            ok = false;
        }
        // reset for subsequent use
        transIndex = 0;
        out = null;
        image = null;
        pixels = null;
        indexedPixels = null;
        colorTab = null;
        closeStream = false;
        firstFrame = true;
        return ok;
    }

    protected void getImagePixels() {
        int w = this.image.getWidth();
        int h = this.image.getHeight();
        Bitmap.Config localConfig = Bitmap.Config.ARGB_8888;
        Bitmap localBitmap1 = Bitmap.createBitmap(w, h, localConfig);
        Canvas localCanvas = new Canvas(localBitmap1);
        localCanvas.save();
        Paint localPaint = new Paint();
        localCanvas.drawBitmap(image, 0, 0, localPaint);
        localCanvas.restore();

        this.pixels = new byte[w * h * 3];
        int[] arrayOfInt = new int[w * h];
        int k = 0;
        int l = 0;
        int i1 = w;
        localBitmap1.getPixels(arrayOfInt, 0, w, k, l, i1, h);
        int localObject = 0;
        while (true) {

            if (localObject >= arrayOfInt.length)
                return;
            pixels[localObject * 3] = (byte) Color.blue(arrayOfInt[localObject]);
            pixels[localObject * 3 + 1] = (byte) Color.green(arrayOfInt[localObject]);
            pixels[localObject * 3 + 2] = (byte) Color.red(arrayOfInt[localObject]);
            ++localObject;
        }
    }

    public void setDelay(int ms) {
        delay = Math.round(ms / 10.0f);
    }

    public void setDispose(int code) {
        if (code >= 0) {
            dispose = code;
        }
    }

    public void setFrameRate(float fps) {
        if (fps != 0f) {
            delay = Math.round(100f / fps);
        }
    }

    public void setQuality(int quality) {
        if (quality < 1)
            quality = 1;
        sample = quality;
    }

    public void setRepeat(int iter) {
        if (iter >= 0) {
            repeat = iter;
        }
    }

    public void setSize(int w, int h) {
        if (started && !firstFrame)
            return;
        width = w;
        height = h;
        //TODO:修改，原来是320和240
        if (width < 1)
            width = 160;
        if (height < 1)
            height = 120;
        sizeSet = true;
    }

    public void setTransparent(int c) {
        this.transparent = c;
    }

    public boolean start(OutputStream os) {
        if (os == null)
            return false;
        boolean ok = true;
        closeStream = false;
        out = os;
        Log.d("AnimatedGifEncode is start outputSteam");
        try {
            writeString("GIF89a"); // header
        } catch (IOException e) {
            ok = false;
        }
        return started = ok;
    }

    public boolean start(String file) {
        boolean ok = true;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            ok = start(out);
            Log.d("AnimatedGifEncode is start =" + file);
            closeStream = true;
        } catch (IOException e) {
            ok = false;
        }
        return started = ok;
    }

    protected void writeGraphicCtrlExt() throws IOException {
        out.write(0x21); // extension introducer
        out.write(0xf9); // GCE label
        out.write(4); // data block size
        int transp, disp;
        if (transparent == 0) {
            transp = 0;
            disp = 0; // dispose = no action
        } else {
            transp = 1;
            disp = 2; // force clear if using transparent color
        }
        if (dispose >= 0) {
            disp = dispose & 7; // user override
        }
        disp <<= 2;
        // packed fields
        out.write(0 | // 1:3 reserved
                disp | // 4:6 disposal
                0 | // 7 user input - 0 = none
                transp); // 8 transparency flag
        writeShort(delay); // delay x 1/100 sec
        out.write(transIndex); // transparent color index
        out.write(0); // block terminator
    }

    protected void writeImageDesc() throws IOException {
        out.write(0x2c); // image separator
        writeShort(0); // image position x,y = 0,0
        writeShort(0);
        writeShort(width); // image size
        writeShort(height);
        // packed fields
        if (firstFrame) {
            // no LCT - GCT is used for first (or only) frame
            out.write(0);
        } else {
            // specify normal LCT
            out.write(0x80 | // 1 local color table 1=yes
                    0 | // 2 interlace - 0=no
                    0 | // 3 sorted - 0=no
                    0 | // 4-5 reserved
                    palSize); // 6-8 size of color table
        }
    }

    protected void writeLSD() throws IOException {
        // logical screen size
        writeShort(width);
        writeShort(height);
        // packed fields
        out.write((0x80 | // 1 : global color table flag = 1 (gct used)
        0x70 | // 2-4 : color resolution = 7
        0x00 | // 5 : gct sort flag = 0
        palSize)); // 6-8 : gct size
        out.write(0); // background color index
        out.write(0); // pixel aspect ratio - assume 1:1
    }

    protected void writeNetscapeExt() throws IOException {
        out.write(0x21); // extension introducer
        out.write(0xff); // app extension label
        out.write(11); // block size
        writeString("NETSCAPE" + "2.0"); // app id + auth code
        out.write(3); // sub-block size
        out.write(1); // loop sub-block id
        //TODO：so i make repeat = 0;
        writeShort(0); // loop count (extra iterations, 0=repeat forever)
        out.write(0); // block terminator
    }

    protected void writePalette() throws IOException {
        out.write(colorTab, 0, colorTab.length);
        int n = (3 * 256) - colorTab.length;
        for (int i = 0; i < n; i++) {
            out.write(0);
        }
    }

    protected void writePixels() throws IOException {
        LZWEncoder encoder = new LZWEncoder(width, height, indexedPixels, colorDepth);
        encoder.encode(out);
    }

    protected void writeShort(int value) throws IOException {
        out.write(value & 0xff);
        out.write((value >> 8) & 0xff);
    }

    protected void writeString(String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            out.write((byte) s.charAt(i));
            Log.d("AnimatedGifEncode is read header!!!");
        }
    }
}
