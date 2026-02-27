# Namespaces and CGroups

## CGroups

1. See [README](../containers-cgroup-info/README.md) for instructions on building.

2. Start the container: `podman run -it --rm -p 8080:8080 --cpus=1 --memory=1G localhost:5001/dmikusa/containers-cgroup-info`. This sets a 1 CPU and 1G RAM limit on the container.

3. Go to [https://localhost:8080/](). You can view the CGroup information.

4. Now try `podman run -it --rm -p 8080:8080 --cpu-period=100000 --cpu-quota=10000 --memory=512M localhost:5001/dmikusa/containers-cgroup-info`. This will lower the RAM allocation, and it will apply a very strict CPU quota (about 1/10th of a CPU). You'll notice the app starts up way slower. When you refresh the app, you can see that it's being throttle as it starts up because it is going over the quota.

5. Ok, now restart the process with `podman run -it --rm -p 8080:8080 --cpus=1 --memory=24M localhost:5001/dmikusa/containers-cgroup-info`. This should be just enough for the app to start and run. How refresh the screen a bunch of times and it should eventually not return a response. If you look at the process, you'll see that it's been SIGKILL'd. This is because the app tried to exceed the 24M limit that we set and that causes the OOM killer to terminate the process.

## Namespaces

1. Start a container: `podman run -it --privileged -h outer-container localhost:5001/library/ubuntu:noble bash`

2. Run:

    ```
    $ hostname
    outer-container
    $ unshare -u /bin/sh
    $ hostname inner-container
    $ hostname
    inner-container
    $ ps aux
    USER         PID %CPU %MEM    VSZ   RSS TTY      STAT START   TIME COMMAND
    root           1  0.0  0.0   4300  3540 pts/0    Ss   03:29   0:00 bash
    root           7  0.0  0.0   2384  1424 pts/0    S    03:30   0:00 /bin/sh
    root           8  0.0  0.0   7632  3532 pts/0    R+   03:30   0:00 ps aux
    $ exit
    $ hostname
    outer-container
    ```

    This shows how you can start a new process and `unshare` removes it from the UTS namespace, at the same time, you can see processes across the two processes because those share a pid namespace.

    Tools like `unshare` allow you to customize how you isolate processes (remember a container is just an isolated process), picking and choosing which isolations make sense for your workloads.

3. Now run:

    ```
    $ hostname
    outer-container
    $ unshare --pid --fork --mount-proc /bin/sh
    $ hostname inner-container
    $ hostname
    inner-container
    $ ps aux
    USER         PID %CPU %MEM    VSZ   RSS TTY      STAT START   TIME COMMAND
    root           1  0.0  0.0   2384  1432 pts/0    S    02:15   0:00 /bin/sh
    root           2  0.0  0.0   7632  3364 pts/0    R+   02:15   0:00 ps aux
    $ exit
    $ hostname
    inner-container
    ```

    Notice this time, we changed `unshare` to isolate the process space. We need to use `--pid` to isolate the pid namespace, `--fork` to create a new pid 1 process, and --mount-proc to mount a new `/proc` filesystem, so tools like `ps` correctly report the state of the container.

    Also, notice how we did not isolate the UTS namespace, so when we set the hostname inside the container it takes effect in both the inner and outer container. This is another example of how you can mix and match the namespaces that you want or need to isolate to create interesting different effects.

