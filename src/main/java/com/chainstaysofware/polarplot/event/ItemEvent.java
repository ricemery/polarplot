/*
 * Copyright (c) 2017 by Gerrit Grunwald
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

package com.chainstaysofware.polarplot.event;

import com.chainstaysofware.polarplot.data.Item;


public class ItemEvent<T extends Item> {
    private final EventType TYPE;
    private final T         ITEM;


    // ******************** Constructors **************************************
    public ItemEvent(final EventType TYPE) {
        this(null, TYPE);
    }
    public ItemEvent(final T ITEM) {
        this(ITEM, EventType.UPDATE);
    }
    public ItemEvent(final T ITEM, final EventType TYPE) {
        this.ITEM = ITEM;
        this.TYPE = TYPE;
    }


    // ******************** Methods *******************************************
    public T getItem() { return ITEM; }

    public EventType getEventType() { return TYPE; }
}
