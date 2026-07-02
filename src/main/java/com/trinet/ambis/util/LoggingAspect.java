package com.trinet.ambis.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingAspect {
    
    protected static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    public void logBefore(JoinPoint joinPoint) {

        logger.info("********************************************************");
        logger.info("* Logging Before Method with parameters: {} {} " , joinPoint.getSignature().getName() , Arrays.toString(joinPoint.getArgs()));

    }

    public void logAfter(JoinPoint joinPoint) {
       
        logger.info("* Logging After Method: {} " , joinPoint.getSignature().getName() + "()");
        logger.info("*********************************************************");
    }
    
    public Object logAround(ProceedingJoinPoint proceedingJoinPoint) {

        Object ret = null;
        BigDecimal start = BigDecimal.valueOf(System.currentTimeMillis());

        try {
            logger.info("--> Enter Method with parameters: {} {} " , proceedingJoinPoint.getSignature().getName() , Arrays.toString(proceedingJoinPoint.getArgs()));
            logger.info("---- {} " , proceedingJoinPoint);
            
            ret = proceedingJoinPoint.proceed();

            BigDecimal end = BigDecimal.valueOf(System.currentTimeMillis());
            
            BigDecimal result = (end.subtract(start)).divide(BigDecimal.valueOf(1000)).setScale(2, RoundingMode.HALF_UP);
            logger.info("--> Leaving Method: {} " , proceedingJoinPoint.getSignature().getName() + "() " + result + " sec.");

        } catch (Throwable e) {
            logger.info("* After Throwing");
        } finally {
//            logger.info("* After Finally");
        }

        return ret;
    }
}