package io.github.nasso.elektrode.data;

import io.github.nasso.elektrode.model.World;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class WorldCodec {
	public abstract void encode(OutputStream str, World world) throws IOException;
	public abstract World decode(InputStream str) throws IOException;
}
