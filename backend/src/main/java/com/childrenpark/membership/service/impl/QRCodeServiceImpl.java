package com.childrenpark.membership.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.childrenpark.membership.entity.QRCode;
import com.childrenpark.membership.entity.SystemConfig;
import com.childrenpark.membership.entity.User;
import com.childrenpark.membership.mapper.QRCodeMapper;
import com.childrenpark.membership.mapper.SystemConfigMapper;
import com.childrenpark.membership.mapper.UserMapper;
import com.childrenpark.membership.service.QRCodeService;
import com.childrenpark.membership.util.QRCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class QRCodeServiceImpl extends ServiceImpl<QRCodeMapper, QRCode> implements QRCodeService {

    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${qrcode.default-validity-minutes}")
    private int defaultValidityMinutes;

    private static final String QRCODE_VALIDITY_KEY = "qrcode_validity_minutes";
    private static final String QRCODE_PREFIX = "qrcode:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generateQRCode(Long userId) throws Exception {
        int validityMinutes = getQRCodeValidityMinutes();
        String qrcodeData = qrCodeUtils.generateQRCodeData(userId);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusMinutes(validityMinutes);
        
        String qrcodeImage = qrCodeUtils.generateQRCodeBase64(qrcodeData);
        
        QRCode qrCode = new QRCode();
        qrCode.setQrcodeData(qrcodeData);
        qrCode.setUserId(userId);
        qrCode.setCreateTime(now);
        qrCode.setExpireTime(expireTime);
        qrCode.setStatus(1);
        qrCode.setUsed(0);
        save(qrCode);
        
        redisTemplate.opsForValue().set(
                QRCODE_PREFIX + qrcodeData,
                userId,
                validityMinutes,
                TimeUnit.MINUTES
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("qrcodeData", qrcodeData);
        result.put("qrcodeImage", qrcodeImage);
        result.put("isActive", true);
        result.put("expireTime", expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        
        return result;
    }

    @Override
    public Map<String, Object> getCurrentQRCode(Long userId) throws Exception {
        LambdaQueryWrapper<QRCode> wrapper = new LambdaQueryWrapper<QRCode>()
                .eq(QRCode::getUserId, userId)
                .eq(QRCode::getStatus, 1)
                .eq(QRCode::getUsed, 0)
                .gt(QRCode::getExpireTime, LocalDateTime.now())
                .orderByDesc(QRCode::getCreateTime)
                .last("LIMIT 1");
        
        QRCode qrCode = getOne(wrapper);
        
        if (qrCode != null) {
            String qrcodeImage = qrCodeUtils.generateQRCodeBase64(qrCode.getQrcodeData());
            
            Map<String, Object> result = new HashMap<>();
            result.put("qrcodeData", qrCode.getQrcodeData());
            result.put("qrcodeImage", qrcodeImage);
            result.put("isActive", true);
            result.put("expireTime", qrCode.getExpireTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            
            return result;
        }
        
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> refreshQRCode(Long userId) throws Exception {
        LambdaQueryWrapper<QRCode> wrapper = new LambdaQueryWrapper<QRCode>()
                .eq(QRCode::getUserId, userId)
                .eq(QRCode::getStatus, 1)
                .eq(QRCode::getUsed, 0);
        
        QRCode existing = getOne(wrapper);
        if (existing != null) {
            existing.setStatus(0);
            updateById(existing);
            redisTemplate.delete(QRCODE_PREFIX + existing.getQrcodeData());
        }
        
        return generateQRCode(userId);
    }

    @Override
    public QRCode validateQRCode(String qrcodeData) {
        Object cachedUserId = redisTemplate.opsForValue().get(QRCODE_PREFIX + qrcodeData);
        if (cachedUserId == null) {
            return null;
        }
        
        QRCode qrCode = getOne(new LambdaQueryWrapper<QRCode>()
                .eq(QRCode::getQrcodeData, qrcodeData)
                .eq(QRCode::getStatus, 1)
                .eq(QRCode::getUsed, 0)
                .gt(QRCode::getExpireTime, LocalDateTime.now()));
        
        return qrCode;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsUsed(String qrcodeData) {
        QRCode qrCode = getOne(new LambdaQueryWrapper<QRCode>()
                .eq(QRCode::getQrcodeData, qrcodeData));
        
        if (qrCode != null) {
            qrCode.setUsed(1);
            qrCode.setUseTime(LocalDateTime.now());
            updateById(qrCode);
            redisTemplate.delete(QRCODE_PREFIX + qrcodeData);
        }
    }

    @Override
    public int getQRCodeValidityMinutes() {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, QRCODE_VALIDITY_KEY)
                        .eq(SystemConfig::getDeleted, 0)
        );
        
        if (config != null && config.getConfigValue() != null) {
            try {
                return Integer.parseInt(config.getConfigValue());
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        
        return defaultValidityMinutes;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQRCodeValidity(int minutes) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, QRCODE_VALIDITY_KEY)
                        .eq(SystemConfig::getDeleted, 0)
        );
        
        LocalDateTime now = LocalDateTime.now();
        
        if (config == null) {
            config = new SystemConfig();
            config.setConfigKey(QRCODE_VALIDITY_KEY);
            config.setConfigName("二维码有效期");
            config.setDescription("用户动态二维码的有效时间（分钟）");
            config.setCreateTime(now);
            config.setUpdateTime(now);
            config.setDeleted(0);
        }
        
        config.setConfigValue(String.valueOf(minutes));
        config.setUpdateTime(now);
        
        if (config.getId() == null) {
            systemConfigMapper.insert(config);
        } else {
            systemConfigMapper.updateById(config);
        }
    }
}
