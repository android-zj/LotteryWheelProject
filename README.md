# 抽奖转盘 


# 使用

修改你的  `build.gradle`文件

```gradle

//root project
allprojects {
        repositories {
    		...
			maven { url 'https://jitpack.io' }
		}
	}
    
//module project
     dependencies {
            compile 'com.github.android-zj:LotteryWheelProject:1.0.0'
    }
    
```

使用方法

```java


          LotteryWheelView mLotteryView = findViewById(R.id.lottery);
          //开始抽奖
          mLotteryView.start();
          
```
