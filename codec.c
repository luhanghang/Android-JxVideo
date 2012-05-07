#include <jni.h>
#include <android/log.h>
#include "libavcodec/avcodec.h"

#define  LOG_TAG    "libjxcodec"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define MAX_OUTPUT_BUFFER 100000
typedef struct Jxcodec
{
    AVCodec *codec;
    AVCodecContext *ctx;
    AVFrame *picture;
    uint8_t *picture_buf;
}Jxcodec;

jlong 
Java_com_bsht_jxvideo_Jxcodec_create(JNIEnv * env,
										  jobject this,
										  jint type,
										  jint rate,
										  jint width,
										  jint height,
										  jint fps,
										  jint gop)
{

 
    Jxcodec * encoder = NULL;
	int size = width * height;
    /* must be called before using avcodec lib */
    avcodec_init();

    /* register all the codecs */
    avcodec_register_all();
    //LOGI("avcodec configuration (%s) ", avcodec_configuration());

	encoder = malloc( sizeof (Jxcodec));

	if ( encoder == NULL )
	  return 0;
	encoder->codec = avcodec_find_encoder(CODEC_ID_MPEG4);
    if ( encoder->codec == NULL )
	  {
		LOGI("codec not found !");
		return 0;
	  }

	encoder->ctx = avcodec_alloc_context();
	encoder->picture = avcodec_alloc_frame();

	encoder->ctx->bit_rate = rate;
	encoder->ctx->width = width;
	encoder->ctx->height = height;
    encoder->ctx->time_base = (AVRational){1,fps};
    encoder->ctx->gop_size = gop;
    encoder->ctx->pix_fmt = PIX_FMT_YUV420P;
	if (avcodec_open(encoder->ctx,encoder->codec ) < 0 )
	  {
		 LOGI("could not open codec !");
		 av_free(encoder->ctx);
		 free(encoder);
		 return 0;
	  }
    encoder->picture_buf = malloc((size * 3) / 2); /* size for YUV 420 */
    encoder->picture->data[0] = encoder->picture_buf;
    encoder->picture->data[1] = encoder->picture->data[0] + size;
    encoder->picture->data[2] = encoder->picture->data[1] + size / 4;
    encoder->picture->linesize[0] = encoder->ctx->width;
    encoder->picture->linesize[1] = encoder->ctx->width / 2;
    encoder->picture->linesize[2] = encoder->ctx->width / 2;
    return (jlong)encoder;
}
jint Java_com_bsht_jxvideo_Jxcodec_encode(JNIEnv *env,
										  jobject this,
										  jlong handle,
										  jbyteArray input,
										  jbyteArray output,
										  jint output_buffer_size)
{
   Jxcodec * codec = (Jxcodec*)handle;

   int i = 0,bytes = 0;
   int linesize = codec->picture->linesize[0];
   unsigned char * out_buf = (unsigned char*)((jbyte*)(*env)->GetByteArrayElements(env,output,0));
   unsigned char * yuv = (unsigned char*)((jbyte*)(*env)->GetByteArrayElements(env,input,0));
   unsigned char * y = codec->picture->data[0];
   unsigned char * u = codec->picture->data[1];
   unsigned char * v = codec->picture->data[2];

   memcpy(y,yuv,linesize);
   for ( i=0 ; i < linesize / 4  ; i++ )
	 {
	   *(u+i) = *(yuv+linesize+i*2);
	   *(v+i) = *(yuv+linesize+i*2+1);
	 }

   bytes = avcodec_encode_video(codec->ctx, out_buf, output_buffer_size, codec->picture);
   return bytes;
}
jint Java_com_bsht_jxvideo_Jxcodec_destroy(JNIEnv *env,
										   jobject this,
										   jlong handle)
{
  Jxcodec * codec = (Jxcodec*)handle;
  if ( codec != NULL )
   {
	  if ( codec->ctx ){
		avcodec_close (codec->ctx);
		av_free(codec->ctx);
		av_free(codec->picture);
	  }
	free(codec->picture_buf);
	free(codec);
   }	
  return 0;
}

