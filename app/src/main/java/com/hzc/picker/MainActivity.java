package com.hzc.picker;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hzc.widget.picker.file.FilePicker;
import com.hzc.widget.picker.file.FilePickerUiParams;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDestination;
import com.itextpdf.text.pdf.PdfOutline;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvResult;
    private PdfItextUtil pdfItextUtil;
    private ArrayList<String> mList = new ArrayList<>();
    private static final String TAG = "MainActivity";

    private static final int PDF_SAVE_START = 1;// 保存PDF文件的开始意图
    private static final int PDF_SAVE_RESULT = 2;// 保存PDF文件的结果开始意图

    private ProgressDialog myDialog; // 保存进度框

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case PDF_SAVE_START:
                    Log.d(TAG, "handleMessage: PDF_SAVE_START");
                    if (!myDialog.isShowing())
                        myDialog.show();
                    break;

                case PDF_SAVE_RESULT:
                    if (myDialog.isShowing())
                        myDialog.dismiss();
                    Log.d(TAG, "handleMessage: PDF_SAVE_RESULT");
                    Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            return false;
        }
    });

    /**
     * 初始化识别进度框
     */
    private void initProgress() {
        myDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
        myDialog.setIndeterminateDrawable(getResources().getDrawable(
                R.drawable.progress_ocr));
        myDialog.setMessage("正在保存PDF文件...");
        myDialog.setCanceledOnTouchOutside(false);
        myDialog.setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvResult = findViewById(R.id.tv_result);
//        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FilePicker.build(MainActivity.this, 1)
////                        .setOpenFile(new File("sdcard/123/"))
//                        .setPickFileType(FilePickerUiParams.PickType.FILE_OR_FOLDER)
////                        .setMultiPick(new FilePicker.OnMultiPickListener() {
////                            @Override
////                            public void pick(@NonNull List<File> pathList) {
////                                StringBuilder path = new StringBuilder("多选：\n");
////                                for (int i = 0; i < pathList.size(); i++) {
////                                    path.append(pathList.get(i).getAbsolutePath()).append("\n\n");
////                                }
////                                tvResult.setText(path.toString());
////                            }
////
////                            @Override
////                            public void cancel() {
////                                tvResult.setText("取消选择了");
////                            }
////                        })
//                        .setSinglePick(new FilePicker.OnSinglePickListener() {
//                            @RequiresApi(api = Build.VERSION_CODES.N)
//                            @Override
//                            public void pick(@NonNull File path) throws IOException, DocumentException {
//                                StringBuilder filestring = new StringBuilder("多选：\n");
//
//                                mList = getAllDataFileName(path.getAbsolutePath());
//                               // Collections.sort(fileList,new FileComparator());
//                               // fileList.sort(Comparator.naturalOrder());
//
//                                for (int i = 0; i < mList.size(); i++) {
//                                    filestring.append(mList.get(i)).append("\n\n");
//                                }
//                                tvResult.setText(filestring.toString());
//
//                                toPDF(path);
////
////                                try {
////                                    zcc.createPdf(sourceTextPath, sourceDocPath, desFilename,10);
////                                    //zcc.getPdf(7, 10, sourceDocPath, desFilename);
////                                } catch (DocumentException e) {
////                                    e.printStackTrace();
////                                } catch (IOException e) {
////                                    e.printStackTrace();
////                                }
//                            }
//
//                            @Override
//                            public void cancel() {
//                                tvResult.setText("取消选择了");
//                            }
//                        })
//                        .open();
//            }
//        });
        initProgress();
        checkNeedPermissions();

        FilePicker.build(MainActivity.this, 1)
                .setPickFileType(FilePickerUiParams.PickType.FILE_OR_FOLDER)
                .setSinglePick(new FilePicker.OnSinglePickListener() {
                                   @RequiresApi(api = Build.VERSION_CODES.N)
                                   @Override
                                   public void pick(@NonNull File path) throws IOException, DocumentException {
                                       StringBuilder filestring = new StringBuilder("多选：\n");
                                       mList = getAllDataFileName(path.getAbsolutePath());

                                       for (int i = 0; i < mList.size(); i++) {
                                           filestring.append(mList.get(i)).append("\n\n");
                                       }
                                       tvResult.setText(filestring.toString());

                                       toPDF(path);
                                   }
                                    @Override
                                    public void cancel() {
                        tvResult.setText("取消选择了");
                    }
                                   })
                                    .open();;

    }

    //                        .setMultiPick(new FilePicker.OnMultiPickListener() {
//                            @Override
//                            public void pick(@NonNull List<File> pathList) {
//                                StringBuilder path = new StringBuilder("多选：\n");
//                                for (int i = 0; i < pathList.size(); i++) {
//                                    path.append(pathList.get(i).getAbsolutePath()).append("\n\n");
//                                }
//                                tvResult.setText(path.toString());
//                            }
//
//                            @Override
//                            public void cancel() {
//                                tvResult.setText("取消选择了");
//                            }
//                        })
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FilePicker.onActivityResult(this, requestCode, resultCode, data);
    }



    public ArrayList<String> getAllDataFileName(String collectionPath){

        ArrayList<String> fileList = new ArrayList<>();
//        File file = new File(collectionPath);

        List<File> files = Arrays.asList(new File(collectionPath).listFiles());
        Collections.sort(files, new Comparator< File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (File f:files) {
            System.out.println(f.getName());
            String fileName = collectionPath+"/"+f.getName();
            if (fileName.endsWith("jpg")){
                // 文件大小
                // String fileSize = FileSizeUtil.getAutoFileOrFilesSize(tempList[i].toString());
                fileList.add(fileName);
            }
        }

//        File[] tempList = files.listFiles();
       // Collections.sort(tempList, new FileComparator());

//        for (int i = 0; i < tempList.length; i++) {
//            if (tempList[i].isFile()) {
//                System.out.println("文件：" + tempList[i].getName());
//                // tempList[i].toString();// 路径
//                // tempList[i].getName();// 文件名
//                // 文件名
//                String fileName = collectionPath+"/"+tempList[i].getName();
//                if (fileName.endsWith("jpg")){
//                    // 文件大小
//                    // String fileSize = FileSizeUtil.getAutoFileOrFilesSize(tempList[i].toString());
//                    fileList.add(fileName);
//                }
//
//            }
//        }

        return fileList;
    }

    public String toPDF(File PDFpath) {
        Log.d(TAG, "toPDF: PDFpath"+PDFpath);
        Log.d(TAG, "toPDF: PDFpath.getName()"+PDFpath.getName());
        File file = new File(PdfUtils.ADDRESS);
        if (!file.exists())
            file.mkdirs();
        Log.d(TAG, "toPDF: debug2");
//        long time = System.currentTimeMillis();
//        Date date = new Date(time);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
//        final String pdf_address = PdfUtils.ADDRESS + File.separator + "PDF_"
//                + sdf.format(date) + ".pdf";
        final String pdf_address =PDFpath.getPath()+".pdf";

        Log.d(TAG, "toPDF: debug3");
        handler.sendEmptyMessage(PDF_SAVE_START);
        Log.d(TAG, "toPDF: debug4");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "toPDF: debug5");
                    Log.d(TAG, "run: pdf_address："+pdf_address);
                    pdfItextUtil = new PdfItextUtil(pdf_address);
                    //判断是否有图片没有土司提示选择图片
                    //如果有进行合成
                    if (mList.size() > 0) {
                        Log.d(TAG, "run: mList.size："+mList.size());

                        for (int i = 0; i < mList.size(); i++) {
                            Log.d(TAG, "run: mList"+mList.get(i).toString());
                            Bitmap bitmap= BitmapFactory.decodeStream(new FileInputStream(mList.get(i)));
                            //这里当然可以输入文字和标题之类的。我们项目里面是只有图片所以。只需要.addImageToPdfCenterH();当然这里的图片在pdf中的放置可以通过设置的。看工具类
                            pdfItextUtil.addImageToPdfCenterH(mList.get(i), PageSize.A4.getWidth(), PageSize.A4.getHeight());
                            // pdfItextUtil.addTitleToPdf("我是标题"+i).addImageToPdfCenterH(mList.get(i), PageSize.A4.getWidth() - 20, PageSize.A4.getWidth() /bitmap.getWidth() * bitmap.getHeight()).addTextToPdf("这是pdf文件的内容，我这里进行了集成这个Demo，希望可以帮助项目中遇到pdf以及文字合成，我尽自己所能,希望编程路上可以帮助到出来匝道的萌新们");
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "请选选择正确的书籍图片目录", Toast.LENGTH_SHORT).show();
                    }


                    /* pdfItextUtil = new PdfItextUtil(pdf_address)
                            .addTitleToPdf("哈哈哈哈哈哈")
                            .addTextToPdf("小时一宗大都但是你下u狗的两个垃圾啊的佛教是浪费家里睡大觉分类数据分类将军澳隧道连接法兰圣诞节佛山警方拉数据")
                            .addImageToPdfCenterH(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MyPdf" + File.separator + "aa.jpg", PageSize.A4.getWidth()-20,PageSize.A4.getWidth()/bitmap1.getWidth()*bitmap1.getHeight())
                            .addTextToPdf("真滴都是无语打来电话了发几份简历垃圾死了就大了就")
                            .addImageToPdfCenterH(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MyPdf" + File.separator + "bb.jpg",  PageSize.A4.getWidth()-20,PageSize.A4.getWidth()/bitmap2.getWidth()*bitmap2.getHeight())
                            .addTextToPdf("笑死宝宝补偿钱");*/

                    pdfItextUtil.storeOutline(PDFpath.getPath());

                    pdfItextUtil.close();

                    handler.sendEmptyMessage(PDF_SAVE_RESULT);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (pdfItextUtil != null) {
                        pdfItextUtil.close();
                    }
                }
            }
        }).start();

        return pdf_address;
    }

    private void checkNeedPermissions() {
        //6.0以上需要动态申请权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //多个权限一起申请
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
        }
    }

}
