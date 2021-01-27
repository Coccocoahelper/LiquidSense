package net.ccbluex.liquidbounce.utils.render;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public final class WorldToScreen
{

	public static Matrix4f getMatrix(final int matrix)
	{
		final FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);

		GL11.glGetFloat(matrix, floatBuffer);

		return (Matrix4f) new Matrix4f().load(floatBuffer);
	}

//	public static Vector2f worldToScreen(final Vector3f pointInWorld, final int screenWidth, final int screenHeight)
//	{
//		return worldToScreen(pointInWorld, getMatrix(GL11.GL_MODELVIEW_MATRIX), getMatrix(GL11.GL_PROJECTION_MATRIX), screenWidth, screenHeight);
//	}

	public static Vector2f worldToScreen(final Vector3f pointInWorld, final Matrix4f view, final Matrix4f projection, final int screenWidth, final int screenHeight)
	{
		final Vector4f clipSpacePos = multiply(multiply(new Vector4f(pointInWorld.x, pointInWorld.y, pointInWorld.z, 1.0f), view), projection);

		final Vector3f ndcSpacePos = new Vector3f(clipSpacePos.x / clipSpacePos.w, clipSpacePos.y / clipSpacePos.w, clipSpacePos.z / clipSpacePos.w);

//        System.out.println(pointInNdc);

		final float screenX = (ndcSpacePos.x + 1.0f) / 2.0f * screenWidth;
		final float screenY = (1.0f - ndcSpacePos.y) / 2.0f * screenHeight;

		// nPlane = -1, fPlane = 1
		return ndcSpacePos.z < -1.0 || ndcSpacePos.z > 1.0 ? null : new Vector2f(screenX, screenY);

	}

	private static Vector4f multiply(final Vector4f vec, final Matrix4f mat)
	{
		return new Vector4f(vec.x * mat.m00 + vec.y * mat.m10 + vec.z * mat.m20 + vec.w * mat.m30, vec.x * mat.m01 + vec.y * mat.m11 + vec.z * mat.m21 + vec.w * mat.m31, vec.x * mat.m02 + vec.y * mat.m12 + vec.z * mat.m22 + vec.w * mat.m32, vec.x * mat.m03 + vec.y * mat.m13 + vec.z * mat.m23 + vec.w * mat.m33);
	}

	private WorldToScreen()
	{
	}
}
