package com.jacky.mall.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FTPUtil {

    private static Logger log = LoggerFactory.getLogger(FTPUtil.class);

    //ftp对象
    private FTPClient ftp;
    private InputStream is = null;
    private OutputStream os = null;
    private FileOutputStream fos = null;
    private FileInputStream fis = null;

    private boolean login = false;


    public static void main(String[] args) {
        FTPUtil ftpUtil = new FTPUtil();
        ftpUtil.login("192.168.174.130", 21, "jacky", "root");

        boolean b = ftpUtil.uploadFile("/test/test.xlsx", "E:\\person\\idea-workspace\\mall\\src\\main\\resources\\application.yml");

        boolean b1 = ftpUtil.downloadFile("/test/test.xlsx", "E:\\person\\idea-workspace\\mall\\test.yml");
        System.out.println(b1);
    }

    /**
     * @param url  格式 host:port
     * @param name
     * @param pwd
     * @return
     */
    public boolean login(String url, String name, String pwd) {
        String[] split = StringUtils.split(url, ":");
        if (split.length != 2) {
            return false;
        }
        String s = split[1];
        int port = Integer.parseInt(s);
        return login(split[0], port, name, pwd);
    }

    /**
     * 验证登录
     *
     * @param ip
     * @param port
     * @param name
     * @param pwd
     * @return
     */
    public boolean login(String ip, int port, String name, String pwd) {
        try {
            ftp = new FTPClient();
            ftp.connect(ip, port);
            login = ftp.login(name, pwd);
            if (!login) {
                return false;
            }
            // 检测连接是否成功
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                log.error("FTP服务器拒绝连接 ");
                return false;
            }
            ftp.setCharset(StandardCharsets.UTF_8);
            ftp.setControlEncoding("UTF-8");
        } catch (IOException e) {
            log.warn("FTP 登陆失败!", e);
            return false;
        }
        return true;
    }

    public boolean mkdirPath(String path) throws IOException {
        if (!login) {
            log.warn("请先登录FTP！");
            return false;
        }
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String[] split = StringUtils.split(path, "/");
        if (split.length == 0) {
            return false;
        }
        ftp.changeWorkingDirectory("/");
        String d = null;
        for (String name : split) {
            d = new String(name.getBytes("GBK"), StandardCharsets.ISO_8859_1);
            if (ftp.changeWorkingDirectory(d)) {
                continue;
            }
            if (!ftp.makeDirectory(d)) {
                log.error("[FTP]文件夹创建失败！");
                return false;
            }
            ftp.changeWorkingDirectory(d);
        }
        return true;
    }

    /**
     * 获取ftp某一文件（路径）下的文件名字,用于查看文件列表
     *
     * @param remotedir 远程地址目录
     * @return
     */
    public boolean getFilesName(String remotedir) {
        try {
            //获取ftp里面，指定文件夹 里面的文件名字，存入数组中
            FTPFile[] files = ftp.listFiles(remotedir);
            //打印出ftp里面，指定文件夹 里面的文件名字
            for (int i = 0; i < files.length; i++) {
                System.out.println(files[i].getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 上传文件
     *
     * @param remotepath 远程地址文件路径
     * @param localpath  本地文件路径
     * @return
     */
    public boolean uploadFile(String remotepath, String localpath) {
        try {
            if (!login) {
                return false;
            }
            Path path = Paths.get(remotepath);
            Path parent = path.getParent();
            if (parent != null) {
                String dir = parent.toString().replace("\\", "/");
                if (!mkdirPath(dir)) {
                    return false;
                }
            }
            String fileName = new String(path.getFileName().toString().getBytes("GBK"), StandardCharsets.ISO_8859_1);
            return ftp.storeFile(fileName, new FileInputStream(new File(localpath)));
        } catch (IOException e) {
            log.warn("FTP上传文件失败！", e);
            return false;
        }
    }


    /**
     * 下载文件
     *
     * @param remotepath 远程地址文件路径
     * @param localpath  本地文件路径
     * @return
     */
    public boolean downloadFile(String remotepath, String localpath) {
        try {
            if (!login) {
                return false;
            }
            Path path = Paths.get(remotepath);
            Path parent = path.getParent();
            ftp.changeWorkingDirectory("/");
            if (parent != null) {
                String dir = parent.toString().replace("\\", "/");
                dir = new String(dir.getBytes("GBK"), StandardCharsets.ISO_8859_1);
                ftp.changeWorkingDirectory(dir);
            }
            String fileName = new String(path.getFileName().toString().getBytes("GBK"), StandardCharsets.ISO_8859_1);
            return ftp.retrieveFile(fileName, new FileOutputStream(new File(localpath)));
        } catch (IOException e) {
            log.warn("FTP下载文件失败！", e);
            return false;
        }
    }

    public void close() {
        closeFtpConnection();
    }

    /**
     * 销毁ftp连接
     */
    private void closeFtpConnection() {
        login = false;
        if (ftp != null) {
            if (ftp.isConnected()) {
                try {
                    ftp.logout();
                    ftp.disconnect();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

}

