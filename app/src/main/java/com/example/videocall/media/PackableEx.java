package com.example.videocall.media;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
