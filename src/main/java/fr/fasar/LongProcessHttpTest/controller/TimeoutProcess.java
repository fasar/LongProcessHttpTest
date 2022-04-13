package fr.fasar.LongProcessHttpTest.controller;

import fr.fasar.LongProcessHttpTest.service.LongProcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
public class TimeoutProcess {
    @Autowired
    ScheduledExecutorService executorService;

    Object lock = new Object();
    HashMap<UUID, Future<?> > process = new HashMap();
    HashMap<UUID, StopWatch> time = new HashMap();
    HashMap<UUID, Long> start = new HashMap();
    HashMap<UUID, Long> stop = new HashMap();

    @GetMapping("/proc/start")
    @ResponseBody
    public ResponseEntity proc() {
        UUID uuid = UUID.randomUUID();
        int i = 0;
        synchronized (lock) {
            while (process.get(uuid) != null) {
                uuid = UUID.randomUUID();
                if (i > 100) {
                    throw new RuntimeException("Can't generate UUID");
                }
            }
            final UUID uuidFinal = uuid;
            StopWatch value = new StopWatch(uuid.toString());
            Future<?> future = executorService.submit(() -> {
                value.start();
                try {
                    return LongProcService.longTimeProc();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    value.stop();
                    synchronized (lock) {
                        stop.put(uuidFinal, System.nanoTime());
                    }
                }
                return null;
            });
            start.put(uuidFinal, System.nanoTime());
            process.put(uuidFinal, future);
            time.put(uuidFinal, value);
        }

        String body = "{ \"status\":\"ok\", \"uuid\":\""+uuid+"\" }";
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        ResponseEntity responseEntity = new ResponseEntity(body, headers, HttpStatus.ACCEPTED);
        return responseEntity;
    }



    @GetMapping("/proc/status")
    public String status() {
        long now = System.nanoTime();
        StringBuilder sb = new StringBuilder("[\n");
        boolean isFirst = true;
        for (UUID uuid : process.keySet()) {
            Future<?> future; StopWatch stopWatch; Long startNano;
            synchronized (lock) {
                future = process.get(uuid);
                stopWatch = time.get(uuid);
                startNano = start.get(uuid);
            }
            boolean cancelled = future.isCancelled();
            boolean done = future.isDone();
            if (!isFirst) {
                sb.append(",");
            } else {
                isFirst = false;
            }
            sb.append("{ ");
            sb.append(jentry("id", uuid));
            sb.append(",").append(jentry("done", done));
            sb.append(",").append(jentry("cancelled", cancelled));
            if (done || cancelled) {
                sb.append(",").append(jentry("timeSpend", Duration.ofNanos(stopWatch.getTotalTimeNanos())));
                sb.append(",").append(jentry("isRunning", stopWatch.isRunning()));
            } else {
                sb.append(",").append(jentry("timeSpend", Duration.ofNanos(now - startNano)));
                sb.append(",").append(jentry("isRunning", stopWatch.isRunning()));
            }

            if (done && !cancelled) {
                try {
                    Object res = future.get(1, TimeUnit.MILLISECONDS);
                    sb.append(",").append(jentry("result", "YES"));
                } catch (Exception e) {
                    // We should never go in this catch.
                    // If possible, I prefer the stop application to check
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
            if (cancelled) {
                sb.append(",").append(jentry("result", "CANCELED"));
            }

            // Close the jsoin
            sb.append("} \n");
        }
        sb.append("]");
        return sb.toString();
    }

    private String jentry(String key, Object uuid) {
        return "\""+key+"\": \"" + uuid + "\"";
    }


    @GetMapping("/proc/stop")
    public String stop() {
        long now = System.nanoTime();
        StringBuilder sb = new StringBuilder("[\n");
        for (UUID uuid : process.keySet()) {
            Future<?> future; StopWatch stopWatch; Long startNano;
            synchronized (lock) {
                future = process.get(uuid);
                stopWatch = time.get(uuid);
                startNano = start.get(uuid);
            }

            stopTask(future);

            boolean cancelled = future.isCancelled();
            boolean done = future.isDone();

            sb.append("{ \"id\": \"");
            sb.append(uuid.toString());
            sb.append("\", \"done\": \"" + done);
            sb.append("\", \"cancelled\": \"" + cancelled);
            if (done || cancelled) {
                sb.append("\", \"timeSpend\": \"" + Duration.ofNanos(stopWatch.getTotalTimeNanos()));
            } else {
                sb.append("\", \"timeSpend\": \"" + Duration.ofNanos(now - startNano));
            }
            sb.append("\"} \n");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Ask to stop task if running.
     * It's call future.cancel on the thread.
     * The thread throws an InterruptedException if the thread is in sleep but doesn't activate the flag Thread.currentThread().isInterrupted().
     * The thread activate the flag Thread.currentThread().isInterrupted() if it doesn't throw an InterruptedException.
     *
     * To proper stop the task, the task should test Thread.currentThread().isInterrupted() in long loops and throw
     * @param future
     */
    private void stopTask(Future<?> future) {
        try {
            Object o = future.get(1, TimeUnit.NANOSECONDS);
        } catch (Exception e) {
            // Ok, it should raise exception if the task is not finished
        } finally {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }
    }


    @GetMapping("/proc/clear")
    public String clear() {
        synchronized (lock) {
            process.clear();
            time.clear();
            start.clear();
        }

        return "cleared";
    }

}
