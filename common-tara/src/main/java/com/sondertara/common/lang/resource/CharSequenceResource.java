package com.sondertara.common.lang.resource;

import com.sondertara.common.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * {@link CharSequence}资源，字符串做为资源
 *
 * @author huangxiaohu
 * @since 5.5.2
 */
public class CharSequenceResource implements Resource, Serializable {
	private static final long serialVersionUID = 1L;

	private final CharSequence data;
	private final CharSequence name;
	private final Charset charset;

	/**
	 * 构造，使用UTF8编码
	 *
	 * @param data 资源数据
	 */
	public CharSequenceResource(CharSequence data) {
		this(data, null);
	}

	/**
	 * 构造，使用UTF8编码
	 *
	 * @param data 资源数据
	 * @param name 资源名称
	 */
	public CharSequenceResource(CharSequence data, String name) {
		this(data, name, StandardCharsets.UTF_8);
	}

	/**
	 * 构造
	 *
	 * @param data    资源数据
	 * @param name    资源名称
	 * @param charset 编码
	 */
	public CharSequenceResource(CharSequence data, CharSequence name, Charset charset) {
		this.data = data;
		this.name = name;
		this.charset = charset;
	}

	@Override
	public String getName() {
		return StringUtils.str(this.name);
	}

	@Override
	public URL getUrl() {
		return null;
	}

	@Override
	public InputStream getStream() {
		return new ByteArrayInputStream(this.data.toString().getBytes(this.charset));
	}

	@Override
	public String readStr(Charset charset) {
		return this.data.toString();
	}

}
