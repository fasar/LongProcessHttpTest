package fr.fasar.LongProcessHttpTest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

@SpringBootApplication
public class LongProcessHttpTestApplication {

	private static int nbThread = 0;

	public static void main(String[] args) {
		SpringApplication.run(LongProcessHttpTestApplication.class, args);
	}


	@Bean
	ScheduledExecutorService executorService() {
		ThreadFactory threadFactory = new MyThreadFactory();
		return Executors.newScheduledThreadPool(8, threadFactory);
	}

	public static class MyThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			nbThread++;
			return new Thread(r, "MyThreadPool-" + nbThread);
		}
	}

}
