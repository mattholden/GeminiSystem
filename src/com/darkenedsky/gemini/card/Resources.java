package com.darkenedsky.gemini.card;

import com.darkenedsky.gemini.MessageSerializable;


public interface Resources extends MessageSerializable {

	public abstract void add(Resources cost);

	public abstract void remove(Resources cost);

	public abstract boolean isAtLeast(Resources other);

}