package com.kamesuta.mc.signpic.gui;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;

import org.lwjgl.util.Timer;

import com.kamesuta.mc.bnnwidget.WBase;
import com.kamesuta.mc.bnnwidget.WEvent;
import com.kamesuta.mc.bnnwidget.WList;
import com.kamesuta.mc.bnnwidget.WPanel;
import com.kamesuta.mc.bnnwidget.motion.Easings;
import com.kamesuta.mc.bnnwidget.motion.MCoord;
import com.kamesuta.mc.bnnwidget.motion.Motion;
import com.kamesuta.mc.bnnwidget.position.Area;
import com.kamesuta.mc.bnnwidget.position.Coord;
import com.kamesuta.mc.bnnwidget.position.Coords;
import com.kamesuta.mc.bnnwidget.position.PCoord;
import com.kamesuta.mc.bnnwidget.position.Point;
import com.kamesuta.mc.bnnwidget.position.R;
import com.kamesuta.mc.signpic.Client;
import com.kamesuta.mc.signpic.http.Communicator;
import com.kamesuta.mc.signpic.render.RenderHelper;
import com.kamesuta.mc.signpic.state.Progress;
import com.kamesuta.mc.signpic.state.Progressable;
import com.kamesuta.mc.signpic.state.State;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class GuiTask extends WPanel {
	public static final ResourceLocation panel = new ResourceLocation("signpic", "textures/gui/panel.png");
	public static final String ShowPanel = "gui.showpanel";
	public static final String HighlightPanel = "gui.highlight";

	public GuiTask(final R position) {
		super(position);
	}

	@Override
	protected void initWidget() {
		add(new WPanel(new R()) {
			protected boolean oshow;
			protected MCoord oright = MCoord.right(0f);

			protected boolean show;
			protected MCoord right = MCoord.pright(0f);

			@Override
			protected void initWidget() {
				add(new WPanel(new R(PCoord.of(Coords.combine(Coord.pright(-1), this.oright), Coord.pright(0f), this.right))) {
					protected Timer showtime = new Timer();

					public void show(final float j) {
						this.showtime.set(-j);
					}

					@Override
					public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float opacity) {
						final Area a = getGuiPosition(pgp);
						RenderHelper.startShape();
						glColor4f(0f, 0f, 0f, .6f);
						drawRect(a);
						super.draw(ev, pgp, p, frame, opacity);
					}

					@Override
					public void update(final WEvent ev, final Area pgp, final Point p) {
						final Area a = getGuiPosition(pgp);
						if (a.pointInside(p))
							this.showtime.set(-1f);
						final boolean b = this.showtime.getTime()<0f;
						if (b) {
							if (!show) {
								right.stop().add(Easings.easeOutQuart.move(.7f, 1f)).start();
								mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("signpic", "gui.show"), 1.0F));
							}
							show = true;
						} else {
							if (show)
								right.stop().add(Easings.easeOutQuart.move(.7f, 0f)).start();
							show = false;
						}
						if (Client.mc.currentScreen!=null&&!Communicator.instance.getTasks().isEmpty()) {
							if (!oshow)
								oright.stop().add(Easings.easeOutQuart.move(.5f, 2f)).start();
							oshow = true;
						} else {
							if (oshow)
								oright.stop().add(Easings.easeOutQuart.move(.5f, 0f)).start();
							oshow = false;
						}
						super.update(ev, pgp, p);
					}

					@Override
					public boolean mouseClicked(final WEvent ev, final Area pgp, final Point p, final int button) {
						final Area a = getGuiPosition(pgp);
						return super.mouseClicked(ev, pgp, p, button)||a.pointInside(p);
					}

					@Override
					protected void initWidget() {
						add(new WBase(new R(Coord.top(1), Coord.left(1), Coord.width(80), Coord.height(16))) {
							@Override
							public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float opacity) {
								final Area a = getGuiPosition(pgp);
								texture().bindTexture(panel);
								glColor4f(1, 1, 1, 1);
								RenderHelper.startTexture();
								drawTexturedModalRect(a);
							}
						});

						add(new WList<Progressable, TaskElement>(new R(Coord.top(16)), Communicator.instance.getTasks()) {
							@Override
							protected TaskElement createWidget(final Progressable t, final int i) {
								final Object j = t.getState().getMeta().get(ShowPanel);
								if (j instanceof Number)
									show(((Number) j).floatValue());
								final MCoord top = MCoord.top(i*15);
								return new TaskElement(new R(top, Coord.height(15)), top, t);
							}

							@Override
							protected void onMoved(final Progressable t, final TaskElement w, final int from, final int to) {
								w.top.stop().add(Motion.blank(.75f)).add(Easings.easeInCirc.move(.25f, to*15)).start();
							};
						});
					}

					class TaskElement extends WPanel {
						public final MCoord top;

						protected MCoord right;

						protected MCoord opacity;

						@Override
						protected void initPosition(final R position) {
							super.initPosition(position.child(this.right = MCoord.pright(-1f).add(Easings.easeOutQuart.move(.5f, 0f)).start()));
						}

						@Override
						protected void initOpacity() {
							super.setOpacity(this.opacity = new MCoord(1f));
						}

						State state;
						Progress progress;

						public TaskElement(final R position, final MCoord top, final Progressable progressable) {
							super(position);
							this.top = top;
							this.state = progressable.getState();
							this.progress = progressable.getState().getProgress();
						}

						@Override
						public boolean onCloseRequest() {
							if (!show&&this.state.getMeta().get(HighlightPanel)!=null)
								this.right.stop().add(Easings.easeOutQuart.move(2f, 1f)).start();
							this.opacity.stop().add(Easings.easeLinear.move(1f, 0f)).start();
							return false;
						}

						@Override
						public boolean onClosing(final WEvent ev, final Area pgp, final Point p) {
							return this.opacity.isFinished();
						}

						@Override
						protected void initWidget() {
							add(new WPanel(R.diff(1, 1, 0, 0)) {
								@Override
								protected void initWidget() {
									add(new WBase(new R(Coord.left(5f), Coord.top(2), Coord.height(font().FONT_HEIGHT), Coord.right(2))) {
										@Override
										public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float popacity) {
											final Area a = getGuiPosition(pgp);
											glPushMatrix();
											glTranslatef(a.x1(), a.y1(), 0f);
											glScalef(.5f, .5f, .5f);
											final String cont = "...";
											final int contwidth = font().getStringWidth(cont);
											final String name = TaskElement.this.state.getName();
											final int namewidth = font().getStringWidth(name);
											String res;
											final float prefwidth = a.w()*2;
											if (namewidth<prefwidth)
												res = name;
											else
												res = font().trimStringToWidth(name, (int) (prefwidth-contwidth))+cont;
											RenderHelper.startTexture();
											drawString(res, 0f, 0f, new Color(1f, 1f, 1f, Math.max(.05f, getGuiOpacity(popacity)*1f)).getRGB());
											glPopMatrix();
										}
									});

									add(new WPanel(new R(Coord.left(4f), Coord.top(font().FONT_HEIGHT/2+3.8f), Coord.bottom(1.8f), Coord.right(2))) {
										protected MCoord progresscoord = MCoord.pleft(0f);

										@Override
										protected void initWidget() {
											add(new WBase(new R(Coord.pleft(0f), this.progresscoord)) {
												@Override
												public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float popacity) {
													final Area a = getGuiPosition(pgp);
													glColor4f(0f, 78f/256f, 192f/256f, getGuiOpacity(popacity)*1f);
													RenderHelper.startShape();
													drawRect(a);

													glPushMatrix();
													final String prog = String.format("%.1f%%", TaskElement.this.progress.getProgress()*100);
													final int progwidth = font().getStringWidth(prog);
													final float maxx = pgp.x2()*2-progwidth;
													glColor4f(1f, 1f, 1f, getGuiOpacity(popacity)*1f);
													glTranslatef(Math.min(a.x2()+1, maxx/2-1), a.y1(), 0f);
													glScalef(.5f, .5f, .5f);
													RenderHelper.startTexture();
													drawString(prog, 0f, 0f, new Color(1f, 1f, 1f, Math.max(.05f, getGuiOpacity(popacity)*1f)).getRGB());
													glPopMatrix();

													super.draw(ev, pgp, p, frame, popacity);
												}
											});
										}

										@Override
										public void update(final WEvent ev, final Area pgp, final Point p) {
											this.progresscoord.stop().add(Easings.easeOutQuart.move(.1f, TaskElement.this.progress.getProgress())).start();
										}

										@Override
										public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float opacity) {
											final Area a = getGuiPosition(pgp);
											RenderHelper.startShape();
											glColor4f(0f, 0f, 0f, getGuiOpacity(opacity)*0.8f);
											drawRect(a);
											super.draw(ev, pgp, p, frame, opacity);
										}
									});
								}

								@Override
								public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float opacity) {
									final Area area = getGuiPosition(pgp);
									RenderHelper.startShape();
									if (area.pointInside(p))
										glColor4f(.75f, .75f, .75f, getGuiOpacity(opacity)*.125f);
									else
										glColor4f(.5f, .5f, .5f, getGuiOpacity(opacity)*.125f);
									drawRect(area);
									super.draw(ev, pgp, p, frame, opacity);
								}
							});
						}
					}
				});
			}
		});

	}
}