package fr.fasar.LongProcessHttpTest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

@RestController
public class LongProcController {

    @GetMapping("/")
    public String index() {
        return "{ \"status\":\"ok\"}";
    }


    @GetMapping("/longP")
    public String longP() {
        BigInteger nbr3 = calculate();
        return "{ \"status\":\"ok\", \"status\": \""+nbr3+"\"}";
    }

    private BigInteger calculate() {
        BigInteger nbr1=BigInteger.valueOf(0), nbr2=BigInteger.valueOf(1), nbr3=BigInteger.valueOf(0), count=BigInteger.valueOf(500_000);
        //La boucle commence par 2 car 0 et 1 sont deja affiches
        for(BigInteger i=BigInteger.valueOf(2); i.compareTo(count)<0; i = i.add(BigInteger.ONE)) {
            nbr3 = nbr1.add(nbr2);
            nbr1 = nbr2;
            nbr2 = nbr3;
        }
        return nbr3;
    }


    @GetMapping("/longT")
    public String longT() {
        long start = System.currentTimeMillis();
        BigInteger nbr3 = calculate2();
        long stop = System.currentTimeMillis();
        Duration time = Duration.ofMillis(stop - start);
        return "{ \"status\":\"ok\", \"status\": \""+nbr3+"\", \"time\": \""+time+"\"}";
    }

    private BigInteger calculate2() {
        Thread thread = Thread.currentThread();
        BigInteger nbr1=BigInteger.valueOf(0), nbr2=BigInteger.valueOf(1), nbr3=BigInteger.valueOf(0), count=BigInteger.valueOf(500_000);
        //La boucle commence par 2 car 0 et 1 sont deja affiches
        for (BigInteger i = BigInteger.valueOf(2); i.compareTo(count) < 0; i = i.add(BigInteger.ONE)) {
            if (thread.isInterrupted()) {
                throw new RuntimeException("Interrupted");
            }
            nbr3 = nbr1.add(nbr2);
            nbr1 = nbr2;
            nbr2 = nbr3;
        }
        return nbr3;
    }

}
