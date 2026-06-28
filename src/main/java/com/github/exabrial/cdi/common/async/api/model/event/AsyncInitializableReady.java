package com.github.exabrial.cdi.common.async.api.model.event;

import java.io.Serializable;

import com.github.exabrial.cdi.common.async.api.AsyncInitializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsyncInitializableReady implements Serializable {
	private static final long serialVersionUID = 1L;

	private Class<? extends AsyncInitializable> initializableType;
}
