package com.wsw.fitnesssystem.data_exchange.interfaces.web.adapter;

import com.wsw.fitnesssystem.data_exchange.application.dto.upload.UploadedFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 将 Spring {@link MultipartFile} 适配为应用层 {@link UploadedFile}。
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 01:37
 * @since 1.0
 */
public class MultipartUploadedFile implements UploadedFile {

    private final MultipartFile delegate;

    private MultipartUploadedFile(MultipartFile delegate) {
        this.delegate = delegate;
    }

    public static UploadedFile of(MultipartFile file) {
        return new MultipartUploadedFile(file);
    }

    @Override
    public String getOriginalFilename() {
        return delegate.getOriginalFilename();
    }

    @Override
    public long getSize() {
        return delegate.getSize();
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return delegate.getInputStream();
    }

}
