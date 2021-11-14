package com.hzc.picker;

import android.os.Environment;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PdfUtils {
    public static final String ADDRESS = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"MyPdf";

    public static class Config {
        public static final boolean DEVELOPER_MODE = false;
    }

    public static String[] getString() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_UNMOUNTED)) {
            return null;
        }
        List<File> fileList = new ArrayList<File>();
        String[] string = null;
        String path = ADDRESS;
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].getAbsolutePath().endsWith(".pdf")) {
                    fileList.add(files[i]);
                }
            }
            Collections.sort(fileList, new FileComparator());
            string = new String[fileList.size()];
            for (int i = 0; i < string.length; i++) {
                string[i] = fileList.get(i).getAbsolutePath().toString();
            }
        }
        return string;
    }

    /**
     * 将文件按时间升序排列
     */
    static class FileComparator implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            if (lhs.lastModified() < rhs.lastModified()) {
                return 1;// 最后修改的照片在前
            } else {
                return -1;
            }
        }
    }
    public void imgTransformPdf(String[] imgPaths, String pdf_save_address){
        Document doc = new Document(PageSize.A4, 0, 0, 0, 0);
        try {
            //获取PDF书写器
            PdfWriter.getInstance(doc, new FileOutputStream(pdf_save_address));
            //打开文档
            doc.open();
            //图片对象
            Image img  = null;
            //遍历
            for (int i = 0; i < imgPaths.length; i++) {
                //获取图片
                img = Image.getInstance(new URL(imgPaths[i]));
                //使图片与A4纸张大小自适应
                img.scaleToFit(new Rectangle(PageSize.A4));
                //添加到PDF文档
                doc.add(img);
                //下一页，每张图片一页
                doc.newPage();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            //关闭文档
            doc.close();
        }

    }
}
