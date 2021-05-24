package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Vector;

public class Main {
    static int oldW;
    static int oldH;
    static Vector<MATRIX> dataOfFile = new Vector<>();
    static int[] dataSender;
    static int [][] dataFile;

    static Vector<MATRIX> CreateBlocks(int[][] SquaredImage) {
        Vector <MATRIX> Vectors = new Vector <>();

        for (int i = 0; i < oldH; i += 2) {
            for (int j = 0; j < oldW; j += 2) {
                int[][] x = new int[2][2];
                x[0][0] = SquaredImage[i][j];
                x[0][1] = SquaredImage[i][j + 1];
                x[1][0] = SquaredImage[i + 1][j];
                x[1][1] = SquaredImage[i + 1][j + 1];
                MATRIX m = new MATRIX(x);
                Vectors.add(m);
            }
        }
        dataSender = new int[Vectors.size()];
        return Vectors;
    }

    public static int[][] readImage(String filePath) {

        File f = new File(filePath);

        int[][] imageMAtrix = null;

        try {
            BufferedImage img = ImageIO.read(f);
            oldW = img.getWidth();
            oldH = img.getHeight();
            if(oldW%2 != 0){
                oldW = ((oldW/2)+1)*2;
            }
            if(oldH%2 != 0){
                oldH = ((oldH/2)+1)*2;
            }
            imageMAtrix = new int[oldH][oldW];

            for (int y = 0; y < oldH; y++) {
                for (int x = 0; x < oldW; x++) {
                    if(x >= img.getWidth() || y >= img.getHeight()){
                        imageMAtrix[y][x] = 0;
                    } else {
                        Color c = new Color(img.getRGB(x, y));
                        imageMAtrix[y][x] = c.getRed();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageMAtrix;
    }

    public static void decompresion(String filePath) throws IOException {
        File file = new File(filePath);
        FileReader fileReader = new FileReader(file);
        BufferedReader br = new BufferedReader(fileReader);
        String str;
        str = br.readLine();
        String[] t = str.split(" ");
        oldW = Integer.parseInt(t[0]);
        oldH = Integer.parseInt(t[1]);
        int S = Integer.parseInt(t[2]);
        Vector<MATRIX> codeBlock = new Vector<>();
        for (int i = 0; i < 32; i++){
            str = br.readLine();
            t = str.split(" ");
            MATRIX m = new MATRIX();
            m.data[0][0] = Integer.parseInt(t[0]);
            m.data[0][1] = Integer.parseInt(t[1]);
            m.data[1][0] = Integer.parseInt(t[2]);
            m.data[1][1] = Integer.parseInt(t[3]);
            codeBlock.add(m);
        }
        int index,l,r;
        while ((str = br.readLine()) != null) {
            index = Integer.parseInt(str);
            l = (index/oldW);
            r = index - (l*oldW);
            dataFile[l*2][r*2] = codeBlock.get(index).data[0][0];
            dataFile[l*2][(r*2)+1] = codeBlock.get(index).data[0][1];
            dataFile[(l*2)+1][r*2] = codeBlock.get(index).data[1][0];
            dataFile[(l*2)+1][(r*2)+1] = codeBlock.get(index).data[1][1];
        }
        writeImage(dataFile,"decompresion.jpg");
    }

    public static void writeImage(int[][] imagePixels, String outPath) {
        BufferedImage img = new BufferedImage(oldW, oldH, BufferedImage.TYPE_3BYTE_BGR);
        for (int y = 0; y < oldH; y++) {
            for (int x = 0; x < oldW; x++) {
                Color c = new Color(imagePixels[y][x],imagePixels[y][x],imagePixels[y][x]);
                img.setRGB(x, y,c.getRGB());
            }
        }

        File f = new File(outPath);

        try {
            ImageIO.write(img, "jpg", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String outPath, Vector<MATRIX> c) throws IOException {
        File file = new File(outPath);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(oldW + " " + oldH + " " +dataOfFile.size() + '\n');
        for (int i = 0; i < 32; i++){
            fileWriter.write( c.get(i).data[0][0] + " " + c.get(i).data[0][1] + " " + c.get(i).data[1][0] + " " + c.get(i).data[1][1] + '\n');
        }
        for (int i = 0; i < dataSender.length; i++){
            fileWriter.write(String.valueOf(dataSender[i]) + '\n');
        }
        fileWriter.close();
    }

    public static Vector<MATRIX> Quantize(Vector<MATRIX> data, Vector<MATRIX> codeblock){
        MATRIX temp = average(data);
        MATRIX newM = new MATRIX();
        newM.data[0][0] = temp.data[0][0] + 1;
        newM.data[0][1] = temp.data[0][1] + 1;
        newM.data[1][0] = temp.data[1][0] + 1;
        newM.data[1][1] = temp.data[1][1] + 1;
        codeblock.add(temp);
        codeblock.add(newM);
        check(codeblock);
        int Size;
        while (codeblock.size() < 32){
            Size = codeblock.size();
            for (int i = 0; i < Size; i++){
                temp = average(codeblock.get(0).child);
                newM = new MATRIX();
                newM.data[0][0] = temp.data[0][0] + 1;
                newM.data[0][1] = temp.data[0][1] + 1;
                newM.data[1][0] = temp.data[1][0] + 1;
                newM.data[1][1] = temp.data[1][1] + 1;
                codeblock.remove(codeblock.get(0));
                codeblock.add(temp);
                codeblock.add(newM);
            }
            check(codeblock);
        }
        boolean x = true;
        while (x){
            x = false;
            for (int i = 31; i >= 0; i--){
                temp = average(codeblock.get(i).child);
                if(!(temp.notEqual(codeblock.get(i)))) {
                    codeblock.remove(codeblock.get(i));
                    codeblock.add(temp);
                    x = true;
                }
            }
            check(codeblock);
        }
        return codeblock;
    }

    public static void check(Vector<MATRIX> m){
        int small = -1, temp, index = 0;
        for(int i = 0; i < dataOfFile.size(); i++){
            for (int j = 0; j < m.size(); j++){
                temp = dataOfFile.get(i).compareTo(m.get(j));
                if(small > temp || small == -1){
                    small = temp;
                    index = j;
                }
            }
            small = -1;
            m.get(index).child.add(dataOfFile.get(i));
            dataSender[i] = index;
        }
    }

    public static MATRIX average(Vector<MATRIX> data){
        int l1 = 0, r1 = 0, l2 = 0, r2 = 0;
        for(int i = 0; i < data.size(); i++){
            l1 += data.get(i).getData()[0][0];
            r1 += data.get(i).getData()[0][1];
            l2 += data.get(i).getData()[1][0];
            r2 += data.get(i).getData()[1][1];
        }
        int [][] x = new int[2][2];
        if(data.size() != 0){
            x[0][0] = l1/data.size();
            x[0][1] = r1/data.size();
            x[1][0] = l2/data.size();
            x[1][1] = r2/data.size();
        }
        return new MATRIX(x);
    }

    public static void compresion(String path) throws IOException {
        dataFile = readImage(path);
        dataOfFile = CreateBlocks(dataFile);
        Vector<MATRIX> code = new Vector<>();
        writeFile("compresion.txt" , Quantize(dataOfFile,code));
    }

    public static void main(String[] args) throws IOException {
        compresion("image5.jpg");
        decompresion("compresion.txt");
    }
}

class MATRIX {
    public int [][] data = new int[2][2];
    public Vector<MATRIX> child = new Vector<>();

    public MATRIX(int[][] data) {
        this.data = data;
    }

    public MATRIX() { }

    public int[][] getData() {
        return data;
    }

    public int compareTo(MATRIX o) {
        int [][] d = o.getData();
        int r = 0;
        r += Math.pow(d[0][0] - data[0][0],2);
        r += Math.pow(d[0][1] - data[0][1],2);
        r += Math.pow(d[1][0] - data[1][0],2);
        r += Math.pow(d[1][1] - data[1][1],2);
        return r;
    }

    public boolean notEqual(MATRIX o){
        for (int i = 0; i < 2; i++){
            for (int j = 0; j < 2; j++){
                if (data[i][j] != o.data[i][j]){
                    return false;
                }
            }
        }
        return true;
    }
}