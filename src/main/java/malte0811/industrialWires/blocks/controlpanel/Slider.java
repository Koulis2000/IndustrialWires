package malte0811.industrialWires.blocks.controlpanel;

import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.panelmodel.PanelUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class Slider extends PanelComponent {
	private static final float WIDTH = .0625F;
	private float length;
	private int color;
	private boolean horizontal;
	private byte out;
	private byte rsChannel;
	private int rsId;
	private Set<BiConsumer<Integer, Byte>> outputs = new HashSet<>();
	public Slider(float length, int color, boolean horizontal, int rsId, byte rsChannel) {
		this();
		this.color = color;
		this.length = length;
		this.horizontal = horizontal;
		this.rsChannel = rsChannel;
		this.rsId = rsId;
	}
	public Slider() {
		super("slider");
	}
	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		nbt.setInteger("color", color);
		nbt.setFloat("length", length);
		if (!toItem) {
			nbt.setByte("output", out);
		}
		nbt.setByte("rsChannel", rsChannel);
		nbt.setInteger("rsId", rsId);
		nbt.setBoolean("horizontal", horizontal);
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		color = nbt.getInteger("color");
		length = nbt.getFloat("length");
		out = nbt.getByte("output");
		rsChannel = nbt.getByte("rsChannel");
		rsId = nbt.getInteger("rsId");
		horizontal = nbt.getBoolean("horizontal");
	}

	@Override
	public List<RawQuad> getQuads() {
		List<RawQuad> ret = new ArrayList<>();
		final float yOff = .001F;
		float xSize = horizontal?length:WIDTH;
		float ySize = horizontal?WIDTH:length;
		PanelUtils.addColoredQuad(ret, new Vector3f(0, yOff, 0), new Vector3f(0, yOff, ySize), new Vector3f(xSize, yOff, ySize), new Vector3f(xSize, yOff, 0),
				EnumFacing.UP, gray);
		float[] color = new float[4];
		color[3] = 1;
		for (int i = 0;i<3;i++) {
			color[i] = ((this.color>>(8*(2-i)))&255)/255F*(.5F+out/30F);
		}
		float val;
		if (horizontal) {
			val = (out/15F)*(length-.0625F);
		} else {
			val = (1-out/15F)*(length-.0625F);
		}
		PanelUtils.addColoredBox(color, gray, null, new Vector3f(horizontal?val:0, 0, horizontal?0:val),
				new Vector3f(.0625F, getHeight(), .0625F), ret, false);
		return ret;
	}

	@Nonnull
	@Override
	public PanelComponent copyOf() {
		Slider ret = new Slider(length, color, horizontal, rsId, rsChannel);
		ret.out = out;
		ret.setX(x);
		ret.setY(y);
		ret.panelHeight = panelHeight;
		return ret;
	}

	private AxisAlignedBB aabb;
	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		if (aabb==null) {
			aabb = new AxisAlignedBB(x, 0, y, x+(horizontal?length:WIDTH), getHeight(), y+(horizontal?WIDTH:length));
		}
		return aabb;
	}

	@Override
	public boolean interactWith(Vec3d hitRelative, TileEntityPanel tile) {
		double pos = horizontal?hitRelative.xCoord:(length-hitRelative.zCoord);
		byte newLevel = (byte)(Math.min(pos*16/length, 15));
		if (newLevel!=out) {
			for (BiConsumer<Integer, Byte> output:outputs) {
				output.accept((int)rsChannel, newLevel);
			}
			out = newLevel;
			tile.markDirty();
			tile.triggerRenderUpdate();
			return true;
		}
		return false;
	}

	@Override
	public void registerRSOutput(int id, @Nonnull BiConsumer<Integer, Byte> out) {
		if (id==rsId) {
			outputs.add(out);
			out.accept((int)rsChannel, this.out);
		}
	}

	@Override
	public void unregisterRSOutput(int id, @Nonnull BiConsumer<Integer, Byte> out) {
		if (id==rsId) {
			outputs.remove(out);
		}
	}

	@Override
	public void update(TileEntityPanel tile) {

	}

	@Override
	public float getHeight() {
		return .0625F/2;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		Slider slider = (Slider) o;

		if (Float.compare(slider.length, length) != 0) return false;
		if (color != slider.color) return false;
		if (horizontal != slider.horizontal) return false;
		if (out != slider.out) return false;
		if (rsChannel != slider.rsChannel) return false;
		return rsId == slider.rsId;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (length != +0.0f ? Float.floatToIntBits(length) : 0);
		result = 31 * result + color;
		result = 31 * result + (horizontal ? 1 : 0);
		result = 31 * result + (int) out;
		result = 31 * result + (int) rsChannel;
		result = 31 * result + rsId;
		return result;
	}
}
