# Deconstructing a Container Image

Container images are just tar archives. You can pull out all of the metadata and layer files by simply extracting the files. This demo walks through fetching a file & pulling it apart.

This demo uses [skopeo](https://github.com/containers/skopeo), which is a tool for interacting with container registries that makes it easy to download a image. You can fetch an image using any tool you like, just make sure the tool you're using outputs the file as an OCI archive, not the older Docker-specific format.

## What's inside an image?

1. Create a working directory. Run `mkdir -p ./out`. Change into this directory.

2. Fetch an image. We're using `ubuntu:jammy` here. Run `skopeo copy --override-os linux --override-arch arm64 docker://localhost:5001/library/ubuntu:jammy oci-archive:ubuntu-jammy.tar` to fetch the image and write the OCI file.

3. Extract the files. Run `mkdir ubuntu-jammy` and then `tar -C ubuntu-jammy -xvf ubuntu-jammy.tar`.

    You end up with:

    ```
    blobs/
    blobs/sha256/
    blobs/sha256/6a62a4157b8775eaf4959cb629e757d32d39d1f4c8ac1b0ddc2510b555cf72f3
    blobs/sha256/b80132958ebf70b3248ea5095ffc3d80f35d1dcd1ef16693ba9898373869ef0f
    blobs/sha256/c36472b3458398be28ecbfebbaac44143c040eae73411baded48a22060d3055b
    index.json
    oci-layout
    ```

4. Start with `jq . ubuntu-jammy/index.json`. This is a high level list of what's in the image. You might have multiple entries if your image has, for example, support for multiple OS/architectures. This has one entry (because of how we downloaded it), so let's grab the sha256 hash for that. We can use the sha256 hash to find that item in the blobs list.

5. So `jq . ubuntu-jammy/blobs/sha256/6a62a4157b8775eaf4959cb629e757d32d39d1f4c8ac1b0ddc2510b555cf72f3` should exist and it should be more JSON data. Open it up. This is the metadata for the image. It tells us where the image config is located (also a blob) and where the layers are located, also blobs. Let's grab the config next.

6. So `jq . ubuntu-jammy/blobs/sha256/b80132958ebf70b3248ea5095ffc3d80f35d1dcd1ef16693ba9898373869ef0f` should exist and it should be more JSON data. This is configuration data for the image. It's what you'd see if you run `docker inspect` on the image (actually a little more than that).

7. The other bit of info we can see from step 5.) is that there's one layer in this image. We can locate that layer data in the same way, grab the sha256 from the JSON output. Then look at `ubuntu-jammy/blobs/sha256/c36472b3458398be28ecbfebbaac44143c040eae73411baded48a22060d3055b`. This will be a tar.gz file with the actual contents of that layer in it.

8. Run `mkdir ubuntu-jammy-layer-1` and then `tar -C ubuntu-jammy-layer-1/ -xvf ubuntu-jammy/blobs/sha256/c36472b3458398be28ecbfebbaac44143c040eae73411baded48a22060d3055b` to extract the files to that location. That's it. Those are the actual contents of the layer (with different permissions, since user/groups are different). If you wanted to get a file out of a layer, you could extract it in this way.

