/*
 * Druid - a distributed column store.
 * Copyright (C) 2012, 2013  Metamarkets Group Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package io.druid.query.aggregation.histogram;

import io.druid.query.aggregation.BufferAggregator;
import io.druid.segment.FloatColumnSelector;

import java.nio.ByteBuffer;

public class ApproximateHistogramBufferAggregator implements BufferAggregator
{
  private final FloatColumnSelector selector;
  private final int resolution;
  private final float lowerLimit;
  private final float upperLimit;

  public ApproximateHistogramBufferAggregator(FloatColumnSelector selector, int resolution, float lowerLimit, float upperLimit)
  {
    this.selector = selector;
    this.resolution = resolution;
    this.lowerLimit = lowerLimit;
    this.upperLimit = upperLimit;
  }

  @Override
  public void init(ByteBuffer buf, int position)
  {
    ByteBuffer mutationBuffer = buf.duplicate();
    mutationBuffer.position(position);

    mutationBuffer.putInt(resolution);
    mutationBuffer.putInt(0); //initial binCount
    for (int i = 0; i < resolution; ++i) {
      mutationBuffer.putFloat(0f);
    }
    for (int i = 0; i < resolution; ++i) {
      mutationBuffer.putLong(0L);
    }

    // min
    mutationBuffer.putFloat(Float.POSITIVE_INFINITY);
    // max
    mutationBuffer.putFloat(Float.NEGATIVE_INFINITY);
  }

  @Override
  public void aggregate(ByteBuffer buf, int position)
  {
    ByteBuffer mutationBuffer = buf.duplicate();
    mutationBuffer.position(position);

    ApproximateHistogram h0 = ApproximateHistogram.fromBytesDense(mutationBuffer);
    h0.offer(selector.get());

    mutationBuffer.position(position);
    h0.toBytesDense(mutationBuffer);
  }

  @Override
  public Object get(ByteBuffer buf, int position)
  {
    ByteBuffer mutationBuffer = buf.duplicate();
    mutationBuffer.position(position);
    return ApproximateHistogram.fromBytes(mutationBuffer);
  }

  @Override
  public float getFloat(ByteBuffer buf, int position)
  {
    throw new UnsupportedOperationException("ApproximateHistogramBufferAggregator does not support getFloat()");
  }


  @Override
  public long getLong(ByteBuffer buf, int position)
  {
    throw new UnsupportedOperationException("ApproximateHistogramBufferAggregator does not support getLong()");
  }

  @Override
  public void close()
  {
    // no resources to cleanup
  }
}
