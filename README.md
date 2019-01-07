## 一、概述
增量更新相较于全量更新的好处不言而喻，利用差分算法获得1.0版本到2.0版本的差分包，这样在安装了1.0的设备上只要下载这个差分包就能够完成由1.0-2.0的更新。比如：

存在一个1.0版本的apk

![apk1.png](https://upload-images.jianshu.io/upload_images/2918620-7dbcb5adbbf76c4a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

然后需要升级到2.0版本，而2.0版本的apk为

![apk2.png](https://upload-images.jianshu.io/upload_images/2918620-32794d883521137c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

这样如果进行全量更新则需要下载完整的76.6M大小的apk文件，进行安装。而如果使用增量更新则只需要下载如下 50.7M的差分包。

![patch.png](https://upload-images.jianshu.io/upload_images/2918620-222d0fa262a633ed.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

下载数据减少了26M。这样做的好处不仅仅在于对于流量的节省。对于用户来说现在流量可能并不值钱，或者使用wifi再进行更新，但是从下载时间能够得到一个良好的优化，同时也减小了服务器的压力。

## 二、实现

需要实现增量更新，现在有各种开源的制作与合并差分包的开源库，比如：bsdiff、hdiff等等。因此我们只需要获得源码来使用即可。

> bsdiff 下载地址：
>
> [http://www.daemonology.net/bsdiff/](http://www.daemonology.net/bsdiff/)
>
> bsdiff 依赖bzip2(zip压缩库)
>
> [http://www.bzip.org/1.0.6/bzip2-1.0.6.tar.gz](http://www.bzip.org/1.0.6/bzip2-1.0.6.tar.gz/)

下载完成后解压：

![bsdiff源码.png](https://upload-images.jianshu.io/upload_images/2918620-ab925b9de6bea980.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

1. bsdiff: 		比较两个文件的二进制数据，生成差分包

2. bspatch：	合并旧的文件与差分包，生成新文件

### 执行make
很显然，bspatch我们需要在Android环境下来执行，而bsdiff 一般会在你的存储服务器当中执行即电脑环境下执行(win或linux)


切到解压后的目录，然后执行make：

![](https://upload-images.jianshu.io/upload_images/2918620-0a1248ec2f278a44.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### Makefile:13: *** missing separator.  Stop.
这时会报错，需要修改Makefile文件，将install：下面的if,endif添加一个缩进：
```
install:
	${INSTALL_PROGRAM} bsdiff bspatch ${PREFIX}/bin
.ifndef WITHOUT_MAN
	${INSTALL_MAN} bsdiff.1 bspatch.1 ${PREFIX}/man/man1
.endif
#上面这段makefile片段显然有问题(lsn9资料，指令必须以tab开头)
#因此需要修改为：
install:
	${INSTALL_PROGRAM} bsdiff bspatch ${PREFIX}/bin
	.ifndef WITHOUT_MAN
	${INSTALL_MAN} bsdiff.1 bspatch.1 ${PREFIX}/man/man1
	.endif
#也就是在 `.if` 和 `.endif` 前加一个 tab
```
#### unknown type name 'u_char'; did you mean 'char'static off_t offtin(u_char *buf)

然后，重新执行make:

![](https://upload-images.jianshu.io/upload_images/2918620-f7c1183bed68182a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

错误很明显，找不到u_char等，因为缺少了头文件

在`bspatch.c`文件中加入
```
#include <sys/types.h>
```
再次make就好了

#### no file found bzlib.h之类的错误

如果出现找不到bzip2 `no file found bzlib.h`之类的错误，则需要先安装bzip2：

Ubuntu:

 ```apt install libbz2-dev ```

Centos:

```yum -y install bzip2-devel.x86_64  ```

Mac:

```brew install bzip2```

最后执行make后没有问题了，就会生成两个bsdiff和bspatch的可执行文件

### bsdiff和bspatch的工具的使用
首先我们准备两个apk，old.apk和new.apk，你可以自己随便写个项目，先运行一次拿到生成的apk作为old.apk；然后修改些代码，或者加一些功能，再运行一次生成new.apk；

- 生成增量文件

```
./bsdiff old.apk new.apk patch
```
这样就生成了一个增量文件patch

- 增量文件和old.apk合并成新的apk

```
./bspatch old.apk new2.apk patch
```
这样就生成一个new2.apk

我们可以使用md5来查看new.apk和new2.apk两个文件的md5值，

 ![md5值的比较](https://upload-images.jianshu.io/upload_images/2918620-e804fdfd8b020714.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

 ## 三、android代码中实现bspatch合并

将bspatch.c文件考入到项目的cpp目录下，因为其还需要bzip2依赖，所以将下载好的bzip2的一些源码也考入到项目中，

> 从下载的bzip2里的Makefile中的OBJS可以看出需要7个源文件文件，因此将这对应的源文件考入到抗美中，然后在将依赖的.h文件也考入项目中

![](https://upload-images.jianshu.io/upload_images/2918620-f65c55a40172e514.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


最终要考入的文件如下：

![](https://upload-images.jianshu.io/upload_images/2918620-8eb2670ebcf167da.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

在项目的CMakeLists.txt文件中把bspatch.c和bzip项目源文件加入其中


```
file(GLOB bzip_source src/main/cpp/bzip/*.c)

add_library(
             native-lib

             SHARED

             src/main/cpp/native-lib.cpp
              src/main/cpp/bspatch.c
              ${bzip_source})
```
此时执行AS的Build下的Make project,发现会报错

![](https://upload-images.jianshu.io/upload_images/2918620-3c75fba9972b81b2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

明明考入的有bzlib.h这个文件，为哈还是报错呢？，其实这个是因为bspatch.c里是以`#include <bzlib.h>`这种形式引入bzlib.h的,我们可以将其引入方式改为`#include "bzip/bzlib.h"`就可以了，但在这里我们用一种不修改源码的方式解决，就是在CMakeLists.txt中加入一句`include_directories(src/main/cpp/bzip)`就可以了。

#### 下面是具体实现

**MainActivity.java**
```
public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView version = (TextView) findViewById(R.id.tv_version);
        version.setText(BuildConfig.VERSION_NAME);
    }

    /**
     *
     * @param oldapk 当前运行的apk
     * @param patch 差分包
     * @param output 合成胡的新的apk
     */
    native void bspatch(String oldapk,String patch,String output);

    public void onUpdate(View view) {
        new MyAsyncTask().execute();
    }

    private class MyAsyncTask extends AsyncTask<Void,Void,File>{

        @Override
        protected File doInBackground(Void... voids) {
            //1、合成apk
            String old = getApplication().getApplicationInfo().sourceDir;

            bspatch(old,"/sdcard/patch","/sdcard/new.apk");
            return new File("/sdcard/new.apk");
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            //2、安装
            Intent i = new Intent(Intent.ACTION_VIEW);
            if(Build.VERSION.SDK_INT<Build.VERSION_CODES.N){
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setDataAndType(Uri.fromFile(file),
                        "application/vnd.android.package-archive");
            }else {
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                String packageName = getApplication().getPackageName();
                Uri contentUri = FileProvider.getUriForFile(MainActivity.this, packageName+ ".fileProvider", file);
                i.setDataAndType(contentUri,"application/vnd.android.package-archive");
            }
            startActivity(i);
        }
    }
}
```
记得开启读写SDCard权限。

**native-lib.cpp**

```
#include <jni.h>
#include <string>

extern "C"{
    //引入bspatch.c里的main方法
    extern int main(int argc,char * argv[]);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_zuo_bsdiff_MainActivity_bspatch(JNIEnv *env, jobject instance, jstring oldapk_,
                                                 jstring patch_, jstring output_) {
    const char *oldapk = env->GetStringUTFChars(oldapk_, 0);
    const char *patch = env->GetStringUTFChars(patch_, 0);
    const char *output = env->GetStringUTFChars(output_, 0);


    int argc = 4;
    char *argv[4] ={"", const_cast<char *>(oldapk),const_cast<char *>(output),const_cast<char *>(patch)};
    main(argc,argv);

    env->ReleaseStringUTFChars(oldapk_, oldapk);
    env->ReleaseStringUTFChars(patch_, patch);
    env->ReleaseStringUTFChars(output_, output);
}
```

因为在android7.0以上调用安装界面需要特殊处理：

在AndroidManifest.xml文件中添加provider，这里的provider介绍可以参考博客：[android 7.0 因为file://引起的FileUriExposedException异常](https://www.jianshu.com/p/55b817530fa3/)

```
 <provider
    android:authorities="com.example.zuo.bsdiff.fileProvider"
    android:name="android.support.v4.content.FileProvider"
    android:exported="false"
    android:grantUriPermissions="true">

    <meta-data android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths"/>
</provider>
```
新建一个file_paths.xml

```
<?xml version="1.0" encoding="utf-8"?>
<resource>
    <paths>
        <external-path name="my_bsdiff" path="" />
    </paths>
</resource>
```

## 四、打包
分别打一个1.0版本的apk包，在打一个2.0版本的apk包，然后使用`./bsdiff app-1.apk app-2.apk patch`生成个差分包，将这个差分包考到/sdcard下，安装旧版本的apk后，更新就可以升级到2.0版本的apk

大致的效果图如下：

![增量更新效果图](https://upload-images.jianshu.io/upload_images/2918620-32f473003cfe4dbc.gif?imageMogr2/auto-orient/strip)