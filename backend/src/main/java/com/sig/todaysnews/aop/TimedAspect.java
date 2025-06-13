package com.sig.todaysnews.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class TimedAspect {
    private static final Logger log = LoggerFactory.getLogger(TimedAspect.class);

    @Around("@annotation(com.sig.todaysnews.annotation.TimeCheck)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        StopWatch sw = new StopWatch(pjp.getSignature().toShortString());
        sw.start();
        Object result = pjp.proceed();
        sw.stop();
        log.info("{}: {}ms", sw.getLastTaskName(), sw.getLastTaskTimeMillis());
        return result;
    }
}
