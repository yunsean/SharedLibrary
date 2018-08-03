package com.dylan.devices.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class MxAudioPlayout {

	public boolean open(int sampleRate, int channel, int bitWidth) {
		int sampleRateInHz = sampleRate;
		int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
		int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
		if (channel == 1)channelConfig = AudioFormat.CHANNEL_OUT_MONO;
		else if (channel == 2)channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
		else {
			Log.e("dylan", "Unsupport audio channel: " + channel);
			return false;
		}
		if (bitWidth == 8)audioFormat = AudioFormat.ENCODING_PCM_8BIT;
		else if (bitWidth == 16)audioFormat = AudioFormat.ENCODING_PCM_16BIT;
		else {
			Log.e("dylan", "Unsupport audio bit width: " + bitWidth);
			return false;
		}
		try {
			close();
			int minBufSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
			mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig, audioFormat, minBufSize, AudioTrack.MODE_STREAM);
			mAudioTrack.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	public void playAudioBlock(byte[] datas, int offset, int length) {
		if (datas == null || datas.length < 1 || mAudioTrack == null)return;
		try {
			mAudioTrack.write(datas, offset, length);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void close() {
		try {
			if (mAudioTrack != null) {
				mAudioTrack.stop();
				mAudioTrack = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private AudioTrack mAudioTrack = null;
}
