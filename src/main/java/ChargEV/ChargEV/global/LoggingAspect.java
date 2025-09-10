package ChargEV.ChargEV.global;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@Aspect
@Slf4j
public class LoggingAspect {

    @Around("execution(* ChargEV.ChargEV.chargingStation.service.ChargingStationService.updateChargingStation(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                long executionTime = System.currentTimeMillis() - startTime;
                log.info("{} executed in {}ms (including commit time)", joinPoint.getSignature(), executionTime);
            }
        });
        return proceed;
    }
}