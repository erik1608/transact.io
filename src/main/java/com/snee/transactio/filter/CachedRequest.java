package com.snee.transactio.filter;

import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CachedRequest extends HttpServletRequestWrapper {
	private final byte[] cachedBody;

	public CachedRequest(HttpServletRequest request) throws IOException {
		super(request);
		InputStream requestInputStream = request.getInputStream();
		this.cachedBody = StreamUtils.copyToByteArray(requestInputStream);
	}

	@Override
	public ServletInputStream getInputStream() {
		return new CachedServletInputStream(this.cachedBody);
	}

	@Override
	public BufferedReader getReader() {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
		return new BufferedReader(new InputStreamReader(byteArrayInputStream));
	}

	public static class CachedServletInputStream extends ServletInputStream {
		private final InputStream cachedBodyInputStream;

		public CachedServletInputStream(byte[] cachedBody) {
			this.cachedBodyInputStream = new ByteArrayInputStream(cachedBody);
		}

		@Override
		public boolean isFinished() {
			try {
				return cachedBodyInputStream.available() == 0;
			} catch (IOException e) {
				return true;
			}
		}

		@Override
		public boolean isReady() {
			try {
				return cachedBodyInputStream.available() != 0;
			} catch (IOException e) {
				return false;
			}
		}

		@Override
		public void setReadListener(ReadListener listener) {
			// Stub
		}

		@Override
		public int read() throws IOException {
			return cachedBodyInputStream.read();
		}
	}
}
