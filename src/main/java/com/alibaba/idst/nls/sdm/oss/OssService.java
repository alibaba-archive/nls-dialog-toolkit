package com.alibaba.idst.nls.sdm.oss;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.idst.nls.sdm.common.ToolkitConstants;
import com.alibaba.idst.nls.sdm.common.ToolkitUtil;
import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jianghaitao
 * @date 2019/11/5
 */
@Component
public class OssService {
    public String getJarPath(String appkey){
        final String jarpath = String.format("%s/jar", appkey);
        return jarpath;
    }

    public String getResourcePath(String appkey){
        final String jarpath = String.format("%s/resources", appkey);
        return jarpath;
    }

    public String getDebugPath(String appkey){
        final String debugPath = String.format("%s/debug/debugInfo", appkey);
        return debugPath;
    }

    private OSSClient buildOssClient(String endpoint, String accessId, String accessKey,
                                     String securityToken) {
        ClientConfiguration fileConfig = new ClientConfiguration();
        fileConfig.setConnectionTimeout(600000);
        fileConfig.setConnectionRequestTimeout(600000);
        return new OSSClient(endpoint, accessId, accessKey, securityToken, fileConfig);
    }

    public void uploadFile(OSSClient ossClient,
                                  File localfile, String ossfile) throws FileNotFoundException {
        InputStream is = new FileInputStream(localfile);
        ossClient.putObject(ToolkitConstants.bucket, ossfile, is);
    }

    public void uploadFile(String endpoint, String accessId, String accessKey, String securityToken,
                                  File localfile, String ossfile) throws FileNotFoundException {
        OSSClient ossClient = buildOssClient(endpoint, accessId, accessKey, securityToken);
        uploadFile(ossClient, localfile, ossfile);
        ossClient.shutdown();
    }

    public boolean uploadJar(String jarpath, JSONObject stcInfo, String appkey){
        String jarName = ToolkitUtil.getFileNameByPath(jarpath);
        if (Objects.isNull(jarName)){
            return false;
        }

        String ossJarPath = getJarPath(appkey) + "/" + jarName;
        try {
            uploadFile(ToolkitConstants.ossEndpoint, stcInfo.getString("access_id"),
                stcInfo.getString("access_key"), stcInfo.getString("token"), new File(jarpath), ossJarPath);
        } catch (FileNotFoundException e) {
            System.out.println("上传jar包失败........");
            return false;
        }
        return true;
    }

    public String readWithStcInfo(JSONObject stcInfo, String ossFilePath){
        String result = readFileContent(ToolkitConstants.ossEndpoint, stcInfo.getString("access_id"),
            stcInfo.getString("access_key"), stcInfo.getString("token"), ossFilePath);
        return result;
    }

    public void writeFileContent(OSSClient client, String ossFilePath, InputStream is){
        client.putObject(ToolkitConstants.bucket, ossFilePath, is);
    }

    public String readFileContent(String endpoint, String accessId, String accessKey, String securityToken,
                                String ossFilePath){
        OSSClient ossClient = buildOssClient(endpoint, accessId, accessKey, securityToken);
        OSSObject ossObject = ossClient.getObject(ToolkitConstants.bucket, ossFilePath);
        InputStream stream = ossObject.getObjectContent();
        String result = (String)(new BufferedReader(new InputStreamReader(stream))).lines().collect(Collectors.joining(System.lineSeparator()));
        return result;
    }

    public void writeFileContent(String endpoint, String accessId, String accessKey, String securityToken,
                                        String ossFilePath, String content){
        OSSClient ossClient = buildOssClient(endpoint, accessId, accessKey, securityToken);
        writeFileContent(ossClient, ossFilePath, new ByteArrayInputStream(content.getBytes()));
        ossClient.shutdown();
    }

    public void uploadDir(OSSClient ossClient, String localPath, String ossPath) throws FileNotFoundException {
        File file = new File(localPath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (Objects.nonNull(files)) {
                int size = files.length;

                for(int i = 0; i < size; ++i) {
                    File tmp = files[i];
                    uploadDir(ossClient, localPath + "/" + tmp.getName(), ossPath + "/" + tmp.getName());
                }
            }
        } else {
            writeFileContent(ossClient, ossPath, new FileInputStream(file));
        }
    }

    public void uploadDir(String endpoint, String accessId, String accessKey, String securityToken,
                                 String localPath, String ossPath) throws FileNotFoundException {
        OSSClient ossClient = buildOssClient(endpoint, accessId, accessKey, securityToken);
        uploadDir(ossClient, localPath, ossPath);
        ossClient.shutdown();
    }

    public boolean uploadResources(String resourcePath, JSONObject stcInfo, String appkey){
        try{
            String ossResourcePath = getResourcePath(appkey);
            uploadDir(ToolkitConstants.ossEndpoint, stcInfo.getString("access_id"),
                stcInfo.getString("access_key"), stcInfo.getString("token"), resourcePath,
                ossResourcePath);
        }catch (Exception e){
            System.out.println("上传资源文件失败........");
            return false;
        }
        return true;
    }
}
