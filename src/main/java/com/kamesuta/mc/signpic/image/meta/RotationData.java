package com.kamesuta.mc.signpic.image.meta;

import java.util.List;
import java.util.ListIterator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.kamesuta.mc.bnnwidget.ShortestFloatFormatter;
import com.kamesuta.mc.signpic.image.meta.ImageRotation.ImageRotate;
import com.kamesuta.mc.signpic.render.OpenGL;

public abstract class RotationData implements IMotionFrame<RotationData>, IComposable {
	public static final float defaultOffset = 4f;

	protected abstract void rotate(final float scale);

	public void rotate() {
		rotate(1f);
	}

	@Override
	public RotationData per(final float per, final RotationData before) {
		return new PerRotationData(this, before, per);
	}

	public static RotationData create(final List<ImageRotate> rotates) {
		final Builder<Rotate> builder = ImmutableList.builder();
		for (final ImageRotate rotate : rotates)
			builder.add(new Rotate(rotate));
		return new AbsRotationData(builder.build());
	}

	private static class AbsRotationData extends RotationData {
		private final ImmutableList<Rotate> rotates;

		private AbsRotationData(final ImmutableList<Rotate> rotates) {
			this.rotates = rotates;
		}

		@Override
		protected void rotate(final float scale) {
			for (final ListIterator<Rotate> it = this.rotates.listIterator(this.rotates.size()); it.hasPrevious();)
				it.previous().rotate(scale);
		}

		@Override
		public String compose() {
			final StringBuilder stb = new StringBuilder();
			for (final Rotate rotate : this.rotates)
				stb.append(rotate.compose());
			return stb.toString();
		}
	}

	private static class PerRotationData extends RotationData {
		private final RotationData after;
		private final RotationData before;
		private final float per;

		private PerRotationData(final RotationData after, final RotationData before, final float per) {
			this.after = after;
			this.before = before;
			this.per = per;
		}

		@Override
		@Deprecated
		public String compose() {
			return this.after.compose();
		}

		@Override
		protected void rotate(final float scale) {
			this.before.rotate((1-this.per)*scale);
			this.after.rotate(this.per*scale);
		}
	}

	public static class Rotate {
		public RotateType type;
		public float rotate;

		protected Rotate(final RotateType type, final float rotate) {
			this.type = type;
			this.rotate = rotate;
		}

		public Rotate(final ImageRotate rotate) {
			this(rotate.type, rotate.rotate);
		}

		public void rotate(final float scale) {
			this.type.rotate(this.rotate*scale);
		}

		public String compose() {
			final float rotate = (this.rotate%8+8)%8;
			if (rotate==0)
				return "";
			else if (rotate==defaultOffset)
				return this.type.name();
			else
				return this.type.name()+ShortestFloatFormatter.format(rotate);
		}
	}

	public static enum RotateType {
		X {
			@Override
			public void rotate(final float f) {
				OpenGL.glRotatef(f*360f/8f, 1f, 0f, 0f);
			}
		},
		Y {
			@Override
			public void rotate(final float f) {
				OpenGL.glRotatef(f*360f/8f, 0f, 1f, 0f);
			}
		},
		Z {
			@Override
			public void rotate(final float f) {
				OpenGL.glRotatef(f*360f/8f, 0f, 0f, 1f);
			}
		},
		;

		public abstract void rotate(float f);
	}
}
