package fr.fasar.LongProcessHttpTest.service;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class LongProcService {

    /**
     * This function calculate the fibonacci number with the sleep of 5 seconds before start to calculate.
     * It check if the current thread is interrupted to stop the calculation if the thread is interrupted.
     * @return
     */
    public static BigInteger longTimeProc() {
        Thread thread = Thread.currentThread();
        // FIRST: Check if the thread is interrupted. When we stop all jobs,
        // Executor will start task in queue with the flag isInterrupted
        if (thread.isInterrupted()) {
            throw new TaskInterruptedException("Interrupted");
        }
        // SECOND: On each Thread.sleep and maybe all async io call you should check the InterruptedException to
        // stop the calculataion
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new TaskInterruptedException("Interrupted", e);
        }

        BigInteger nbr1=BigInteger.valueOf(0), nbr2=BigInteger.valueOf(1), nbr3=BigInteger.valueOf(0);
        long count = 300_000L;
        //La boucle commence par 2 car 0 et 1 sont deja affiches
        for (long i = 2L; i< count; i+=1L) {
            // THIRD: On each loop, check if the thread is interrupted to be stop the calculation if needed.
            if (thread.isInterrupted()) {
                throw new TaskInterruptedException("Interrupted");
            }
            nbr3 = nbr1.add(nbr2);
            nbr1 = nbr2;
            nbr2 = nbr3;
        }
        return nbr3;
    }

}
