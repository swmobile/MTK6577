/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package  com.android.pqtuningtool.ui;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class PointerInfo {

    /**
     * The number of coordinates per vertex. 1..4
     */
    public int mSize;

    /**
     * The type of each coordinate.
     */
    public int mType;

    /**
     * The byte offset between consecutive vertices. 0 means mSize *
     * sizeof(mType)
     */
    public int mStride;
    public Buffer mPointer;
    public ByteBuffer mTempByteBuffer;

    public PointerInfo(int size, int type, int stride, Buffer pointer) {
        mSize = size;
        mType = type;
        mStride = stride;
        mPointer = pointer;
    }

    private int getStride() {
        return mStride > 0 ? mStride : sizeof(mType) * mSize;
    }

    public void bindByteBuffer() {
        mTempByteBuffer = mPointer == null ? null : toByteBuffer(-1, mPointer);
    }

    public void unbindByteBuffer() {
        mTempByteBuffer = null;
    }

    private static int sizeof(int type) {
        switch (type) {
        case GL10.GL_UNSIGNED_BYTE:
            return 1;
        case GL10.GL_BYTE:
            return 1;
        case GL10.GL_SHORT:
            return 2;
        case GL10.GL_FIXED:
            return 4;
        case GL10.GL_FLOAT:
            return 4;
        default:
            return 0;
        }
    }

    private static ByteBuffer toByteBuffer(int byteCount, Buffer input) {
        ByteBuffer result = null;
        boolean convertWholeBuffer = (byteCount < 0);
        if (input instanceof ByteBuffer) {
            ByteBuffer input2 = (ByteBuffer) input;
            int position = input2.position();
            if (convertWholeBuffer) {
                byteCount = input2.limit() - position;
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            for (int i = 0; i < byteCount; i++) {
                result.put(input2.get());
            }
            input2.position(position);
        } else if (input instanceof CharBuffer) {
            CharBuffer input2 = (CharBuffer) input;
            int position = input2.position();
            if (convertWholeBuffer) {
                byteCount = (input2.limit() - position) * 2;
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            CharBuffer result2 = result.asCharBuffer();
            for (int i = 0; i < byteCount / 2; i++) {
                result2.put(input2.get());
            }
            input2.position(position);
        } else if (input instanceof ShortBuffer) {
            ShortBuffer input2 = (ShortBuffer) input;
            int position = input2.position();
            if (convertWholeBuffer) {
                byteCount = (input2.limit() - position)* 2;
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            ShortBuffer result2 = result.asShortBuffer();
            for (int i = 0; i < byteCount / 2; i++) {
                result2.put(input2.get());
            }
            input2.position(position);
        } else if (input instanceof IntBuffer) {
            IntBuffer input2 = (IntBuffer) input;
            int position = input2.position();
            if (convertWholeBuffer) {
                byteCount = (input2.limit() - position) * 4;
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            IntBuffer result2 = result.asIntBuffer();
            for (int i = 0; i < byteCount / 4; i++) {
                result2.put(input2.get());
            }
            input2.position(position);
        } else if (input instanceof FloatBuffer) {
            FloatBuffer input2 = (FloatBuffer) input;
            int position = input2.position();
            if (convertWholeBuffer) {
                byteCount = (input2.limit() - position) * 4;
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            FloatBuffer result2 = result.asFloatBuffer();
            for (int i = 0; i < byteCount / 4; i++) {
                result2.put(input2.get());
            }
            input2.position(position);
        } else if (input instanceof DoubleBuffer) {
            DoubleBuffer input2 = (DoubleBuffer) input;
            int position = input2.position();
            if (convertWholeBuffer) {
                byteCount = (input2.limit() - position) * 8;
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            DoubleBuffer result2 = result.asDoubleBuffer();
            for (int i = 0; i < byteCount / 8; i++) {
                result2.put(input2.get());
            }
            input2.position(position);
        } else if (input instanceof LongBuffer) {
            LongBuffer input2 = (LongBuffer) input;
            int position = input2.position();
            if (convertWholeBuffer) {
                byteCount = (input2.limit() - position) * 8;
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            LongBuffer result2 = result.asLongBuffer();
            for (int i = 0; i < byteCount / 8; i++) {
                result2.put(input2.get());
            }
            input2.position(position);
        } else {
            throw new RuntimeException("Unimplemented Buffer subclass.");
        }
        result.rewind();
        // The OpenGL API will interpret the result in hardware byte order,
        // so we better do that as well:
        result.order(ByteOrder.nativeOrder());
        return result;
    }

    public void getArrayElement(int index, double[] result) {
        if (mTempByteBuffer == null) {
            throw new IllegalArgumentException("undefined pointer");
        }
        if (mStride < 0) {
            throw new IllegalArgumentException("invalid stride");
        }

        int stride = getStride();
        ByteBuffer byteBuffer = mTempByteBuffer;
        int size = mSize;
        int type = mType;
        int sizeofType = sizeof(type);
        int byteOffset = stride * index;

        for (int i = 0; i < size; i++) {
            switch (type) {
            case GL10.GL_BYTE:
            case GL10.GL_UNSIGNED_BYTE:
                result[i] = byteBuffer.get(byteOffset);
                break;
            case GL10.GL_SHORT:
                ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
                result[i] = shortBuffer.get(byteOffset / 2);
                break;
            case GL10.GL_FIXED:
                IntBuffer intBuffer = byteBuffer.asIntBuffer();
                result[i] = intBuffer.get(byteOffset / 4);
                break;
            case GL10.GL_FLOAT:
                FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
                result[i] = floatBuffer.get(byteOffset / 4);
                break;
            default:
                throw new UnsupportedOperationException("unknown type");
            }
            byteOffset += sizeofType;
        }
    }
}
