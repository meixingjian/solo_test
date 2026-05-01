package com.childrenpark.membership.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.childrenpark.membership.entity.QRCode;

import java.util.Map;

public interface QRCodeService extends IService<QRCode> {

    Map<String, Object> generateQRCode(Long userId) throws Exception;

    Map<String, Object> getCurrentQRCode(Long userId) throws Exception;

    Map<String, Object> refreshQRCode(Long userId) throws Exception;

    QRCode validateQRCode(String qrcodeData);

    void markAsUsed(String qrcodeData);

    int getQRCodeValidityMinutes();

    void updateQRCodeValidity(int minutes);
}
