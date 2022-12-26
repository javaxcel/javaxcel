/*
 * Copyright 2022 Javaxcel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.javaxcel.core.converter.handler.impl.net;

import com.github.javaxcel.core.converter.handler.AbstractExcelTypeHandler;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Handler for type of {@link URL}
 *
 * @since 0.8.0
 */
public class URLTypeHandler extends AbstractExcelTypeHandler<URL> {

    public URLTypeHandler() {
        super(URL.class);
    }

    @Override
    protected String writeInternal(URL value, Object... arguments) {
        return value.toString();
    }

    @Override
    public URL read(String value, Object... arguments) throws MalformedURLException {
        return new URL(value);
    }

}
