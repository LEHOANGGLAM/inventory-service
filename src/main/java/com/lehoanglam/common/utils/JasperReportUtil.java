package com.yes4all.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StopWatch;

public class JasperReportUtil {

    private static final Logger log = LoggerFactory.getLogger(JasperReportUtil.class);

    private static Map<String, JasperReport> compiledReportCache = new ConcurrentHashMap<>();

    public static byte[] export(String fileName, Map<String, Object> parameters, Collection<?> listData) {
        try {
            StopWatch stopWatch = DateUtil.initStopWatch();
            JasperReport jasperReport = compiledReportCache.computeIfAbsent(
                fileName,
                key -> {
                    try {
                        InputStream cr = new ClassPathResource(fileName).getInputStream();
                        return JasperCompileManager.compileReport(cr);
                    } catch (IOException | JRException e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }
            );
            log.debug("[LOGGING] Compile jasperReport: {}", DateUtil.calculateTime(stopWatch));
            stopWatch.start("exportReport");
            // Set list data
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listData);

            // Fill data
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            byte[] data = JasperExportManager.exportReportToPdf(jasperPrint);
            log.debug("[LOGGING] export report: {}", DateUtil.calculateTime(stopWatch));
            return data;
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return null;
    }
}
