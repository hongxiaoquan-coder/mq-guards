package com.hk.simba.mq.guards.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Allen
 * @since 2021-07-01
 */
@Slf4j
public class HttpUtils {

    /**
     * HTTP协议POST请求方法
     * @param url url链接
     * @param params 发送参数
     * @param gb 编码格式
     * @return java.lang.String
     */
    public static String httpMethodPost(String url, String params, String gb) throws IOException {
        StringBuilder sb = null;
        HttpURLConnection uc  = null;
        BufferedReader in = null;
        try {
            if (null == gb || "".equals(gb)) {
                gb = "UTF-8";
            }
            sb = new StringBuilder();
            URL urls;
            urls = new URL(url);
            uc = (HttpURLConnection) urls.openConnection();
            uc.setRequestMethod("POST");
            uc.setDoOutput(true);
            uc.setDoInput(true);
            uc.setUseCaches(false);
            uc.setRequestProperty("Content-Type", "application/json");
            uc.connect();
            DataOutputStream out = new DataOutputStream(uc.getOutputStream());
            out.write(params.getBytes(gb));
            out.flush();
            out.close();
            in = new BufferedReader(new InputStreamReader(uc.getInputStream(),
                    gb));
            String readLine = "";
            while ((readLine = in.readLine()) != null) {
                sb.append(readLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(in != null){
                in.close();
            }
            if (uc != null) {
                uc.disconnect();
            }
        }
        return sb.toString();
    }

}
