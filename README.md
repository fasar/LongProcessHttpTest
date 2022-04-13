# LongProcessHttpTest
How to kill a long process with SpringBoot


## Long Processing Job

This application create a jobs that can be stopped.

* You can create long processing jobs with : http://localhost:8000/proc/start.
* You can check the jobs status with: http://localhost:8000/proc/status
* You can stop all jobs with : http://localhost:8000/proc/stop
* You can clear the job list with: http://localhost:8000/proc/clear

Check LongProcService to see how to create long process calculation that can be stopped.


