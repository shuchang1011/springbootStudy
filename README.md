# springbootStudy

# 项目介绍

该项目主要是针对springboot的启动过程进行分析，并结合springboot的一系列拓展实现相应的功能。

# 启动整体过程

首先是项目启动类：

```java
    public static void main(String[] args) {
        SpringApplication.run(MarsApplication.class, args);
    }
    public SpringApplication(Object... sources) {
        //初始化
        initialize(sources);
    }
```

初始化时，会先进行区分环境：非web环境、web环境、reactive环境三种。如下：

```java
    private void initialize(Object[] sources) {
        if (sources != null && sources.length > 0) {
            this.sources.addAll(Arrays.asList(sources));
        }
        //设置servlet环境
        this.webEnvironment = deduceWebEnvironment();
        //获取ApplicationContextInitializer，也是在这里开始首次加载spring.factories文件
        setInitializers((Collection) getSpringFactoriesInstances(
                ApplicationContextInitializer.class));
        //获取监听器，这里是第二次加载spring.factories文件
        setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
        this.mainApplicationClass = deduceMainApplicationClass();
    }
```

来看一下具体环境判断的deduceWebEnvironment()方法：

 

```java
    private WebApplicationType deduceWebApplicationType() {
        if (ClassUtils.isPresent(REACTIVE_WEB_ENVIRONMENT_CLASS, null)
                && !ClassUtils.isPresent(MVC_WEB_ENVIRONMENT_CLASS, null)) {
            return WebApplicationType.REACTIVE;
        }
        for (String className : WEB_ENVIRONMENT_CLASSES) {
            if (!ClassUtils.isPresent(className, null)) {
                return WebApplicationType.NONE;
            }
        }
        return WebApplicationType.SERVLET;
    }
```

这里主要是通过判断REACTIVE相关的字节码是否存在，如果不存在，则web环境即为SERVLET类型。这里设置好web环境类型，在后面会根据类型初始化对应环境。

ApplicationContextInitializer是spring组件spring-context组件中的一个接口，**主要是spring ioc容器刷新之前的一个回调接口，用于处于自定义逻辑**。

**ApplicationContextInitializer主要是为了往spring的容器中注入一些属性**

[ApplicationContextInitializer讲解及使用](https://blog.csdn.net/weixin_43762303/article/details/118723169)

这里监听器为9个：

```java
# Application Listeners
org.springframework.context.ApplicationListener=\
org.springframework.boot.ClearCachesApplicationListener,\
org.springframework.boot.builder.ParentContextCloserApplicationListener,\
org.springframework.boot.context.FileEncodingApplicationListener,\
org.springframework.boot.context.config.AnsiOutputApplicationListener,\
org.springframework.boot.context.config.ConfigFileApplicationListener,\
org.springframework.boot.context.config.DelegatingApplicationListener,\
org.springframework.boot.context.logging.ClasspathLoggingApplicationListener,\
org.springframework.boot.context.logging.LoggingApplicationListener,\
org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener
```

还有1个为：org.springframework.boot.autoconfigure.BackgroundPreinitializer
这10个监听器会贯穿springBoot整个生命周期。稍后会介绍。

这里先继续后面的流程。来看一下run方法：

```java
public ConfigurableApplicationContext run(String... args) {
		//时间监控
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ConfigurableApplicationContext context = null;
		Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
		//java.awt.headless是J2SE的一种模式用于在缺少显示屏、键盘或者鼠标时的系统配置，很多监控工具如jconsole 需要将该值设置为true，系统变量默认为true
		configureHeadlessProperty();
		//获取spring.factories中的监听器变量，args为指定的参数数组，默认为当前类SpringApplication
		//第一步：获取并启动监听器
		SpringApplicationRunListeners listeners = getRunListeners(args);
		listeners.starting();
		try {
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(
					args);
			//第二步：构造容器环境
			ConfigurableEnvironment environment = prepareEnvironment(listeners,
					applicationArguments);
			//设置需要忽略的bean
			configureIgnoreBeanInfo(environment);
			//打印banner
			Banner printedBanner = printBanner(environment);
			//第三步：创建容器
			context = createApplicationContext();
			//第四步：实例化SpringBootExceptionReporter.class，用来支持报告关于启动的错误
			exceptionReporters = getSpringFactoriesInstances(
					SpringBootExceptionReporter.class,
					new Class[] { ConfigurableApplicationContext.class }, context);
			//第五步：准备容器
			prepareContext(context, environment, listeners, applicationArguments,
					printedBanner);
			//第六步：刷新容器
			refreshContext(context);
			//第七步：刷新容器后的扩展接口
			afterRefresh(context, applicationArguments);
			stopWatch.stop();
			if (this.logStartupInfo) {
				new StartupInfoLogger(this.mainApplicationClass)
						.logStarted(getApplicationLog(), stopWatch);
			}
			listeners.started(context);
			callRunners(context, applicationArguments);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, listeners);
			throw new IllegalStateException(ex);
		}
		try {
			listeners.running(context);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, null);
			throw new IllegalStateException(ex);
		}
		return context;
	}
```

- **第一步：获取并启动监听器**
- **第二步：构造容器环境**
- **第三步：创建容器**
- **第四步：实例化SpringBootExceptionReporter.class，用来支持报告关于启动的错误**
- **第五步：准备容器**
- **第六步：刷新容器**
- **第七步：刷新容器后的扩展接口**

## 一：获取并启动监听器

## 1）获取监听器

`SpringApplicationRunListeners listeners = getRunListeners(args);`
跟进`getRunListeners`方法：

```java
	private SpringApplicationRunListeners getRunListeners(String[] args) {
		Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
		return new SpringApplicationRunListeners(logger, getSpringFactoriesInstances(
				SpringApplicationRunListener.class, types, this, args));
	}
```

上面可以看到，args本身默认为空，但是在获取监听器的方法中，getSpringFactoriesInstances( SpringApplicationRunListener.class, types, this, args)将当前对象作为参数，该方法用来获取spring.factories对应的监听器：

```java
# Run Listeners
org.springframework.boot.SpringApplicationRunListener=\
org.springframework.boot.context.event.EventPublishingRunListener
```

整个 springBoot 框架中获取factories的方式统一如下：

```java
	@SuppressWarnings("unchecked")
	private <T> List<T> createSpringFactoriesInstances(Class<T> type,
			Class<?>[] parameterTypes, ClassLoader classLoader, Object[] args,
			Set<String> names) {
		List<T> instances = new ArrayList<>(names.size());
		for (String name : names) {
			try {
				//装载class文件到内存
				Class<?> instanceClass = ClassUtils.forName(name, classLoader);
				Assert.isAssignable(type, instanceClass);
				Constructor<?> constructor = instanceClass
						.getDeclaredConstructor(parameterTypes);
				//主要通过反射创建实例
				T instance = (T) BeanUtils.instantiateClass(constructor, args);
				instances.add(instance);
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException(
						"Cannot instantiate " + type + " : " + name, ex);
			}
		}
		return instances;
	}
```

上面通过反射获取实例时会触发`EventPublishingRunListener`的构造函数：

```java
	public EventPublishingRunListener(SpringApplication application, String[] args) {
		this.application = application;
		this.args = args;
		this.initialMulticaster = new SimpleApplicationEventMulticaster();
		for (ApplicationListener<?> listener : application.getListeners()) {
			this.initialMulticaster.addApplicationListener(listener);
		}
	}
```

重点来看一下`addApplicationListener`方法：

```java
	@Override
	public void addApplicationListener(ApplicationListener<?> listener) {
		synchronized (this.retrievalMutex) {
			// Explicitly remove target for a proxy, if registered already,
			// in order to avoid double invocations of the same listener.
			Object singletonTarget = AopProxyUtils.getSingletonTarget(listener);
			if (singletonTarget instanceof ApplicationListener) {
				this.defaultRetriever.applicationListeners.remove(singletonTarget);
			}
			//内部类对象
			this.defaultRetriever.applicationListeners.add(listener);
			this.retrieverCache.clear();
		}
	}
```

 上述方法定义在SimpleApplicationEventMulticaster父类AbstractApplicationEventMulticaster中。关键代码为this.defaultRetriever.applicationListeners.add(listener);，这是一个内部类，用来保存所有的监听器。也就是在这一步，将spring.factories中的监听器传递到SimpleApplicationEventMulticaster中。

[ApplicationEventMulticaster事件多播器讲解及使用](https://blog.csdn.net/weixin_43762303/article/details/118757849)
继承关系如下：
![img](https://img-blog.csdnimg.cn/20210715150729829.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzc2MjMwMw==,size_16,color_FFFFFF,t_70)

##  2）启动监听器

`listeners.starting();`,获取的监听器为`EventPublishingRunListener`，从名字可以看出是启动事件发布监听器，主要用来发布启动事件。

```java
	@Override
	public void starting() {
	//关键代码，这里是创建application启动事件`ApplicationStartingEvent`
		this.initialMulticaster.multicastEvent(
				new ApplicationStartingEvent(this.application, this.args));
	}
```

`EventPublishingRunListener`这个是springBoot框架中最早执行的监听器，在该监听器执行`started()`方法时，会继续发布事件，也就是事件传递。这种实现主要还是基于spring的事件机制。
继续跟进`SimpleApplicationEventMulticaster`，有个核心方法：

```java
	@Override
	public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
		ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
		for (final ApplicationListener<?> listener : getApplicationListeners(event, type)) {
			//获取线程池，如果为空则同步处理。这里线程池为空，还未没初始化。
            //可以参考上述ApplicationMulticaster讲解及使用
			Executor executor = getTaskExecutor();
			if (executor != null) {
			    //异步发送事件
				executor.execute(() -> invokeListener(listener, event));
			}
			else {
				//同步发送事件
				invokeListener(listener, event);
			}
		}
	}
```

这里会根据事件类型`ApplicationStartingEvent`获取对应的监听器，在容器启动之后执行响应的动作，有如下5种监听器： 

![img](https://img-blog.csdnimg.cn/20210715152548743.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzc2MjMwMw==,size_16,color_FFFFFF,t_70)这是springBoot启动过程中，第一处根据类型，执行监听器的地方。根据发布的事件类型从上述5种监听器中选择对应的监听器进行事件发布，当然如果继承了 springCloud或者别的框架，就不止5个了。这里选了一个 springBoot 的日志监听器来进行讲解，核心代码如下：

```java
 @Override
    public void onApplicationEvent(ApplicationEvent event) {
        //在springboot启动的时候
        if (event instanceof ApplicationStartedEvent) {
            onApplicationStartedEvent((ApplicationStartedEvent) event);
        }
        //springboot的Environment环境准备完成的时候
        else if (event instanceof ApplicationEnvironmentPreparedEvent) {
            onApplicationEnvironmentPreparedEvent(
                    (ApplicationEnvironmentPreparedEvent) event);
        }
        //在springboot容器的环境设置完成以后
        else if (event instanceof ApplicationPreparedEvent) {
            onApplicationPreparedEvent((ApplicationPreparedEvent) event);
        }
        //容器关闭的时候
        else if (event instanceof ContextClosedEvent && ((ContextClosedEvent) event)
                .getApplicationContext().getParent() == null) {
            onContextClosedEvent();
        }
        //容器启动失败的时候
        else if (event instanceof ApplicationFailedEvent) {
            onApplicationFailedEvent();
        }
    }
```

因为我们的事件类型为`ApplicationEvent`，所以会执行`onApplicationStartedEvent((ApplicationStartedEvent) event);`。springBoot会在运行过程中的不同阶段，发送各种事件，来执行对应监听器的对应方法（不同的监听器实现不同的监听事件）。大同小异，别的监听器执行流程这里不再赘述，后面会有单独的详解。
继续后面的流程。

## 3）自定义监听器实现

在第一小节，获取监听器`getRunListeners`中，可以看到，springboot加载监听器是通过读取spring.factories的`org.springframework.boot.SpringApplicationRunListener`的配置来决定加载哪些监听器的。

因此，我们可以通过自定义实现一个监听器，然后在spring.factories中加入指定的监听器。在构建监听器的过程中，需要实现`ApplicationListener`接口，`ApplicationListener`是springboot中监听器的一个声明接口。或者实现其继承类`SmartApplicationListener`。

它还提供了`supportsEventType(Class<? extends ApplicationEvent>)`方法，方便用户对于监听事件类型进行过滤。

![SpringApplicationEvent类图](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211223151908164.png)

同时，还可以自定义事件。springboot自身定义了生命周期中各个阶段的的触发事件，其统一继承了ApplicationEvent类，在触发事件调用`onApplicationStartedEvent(ApplicationStartedEvent event)`，也是获取ApplicationEvent类型的事件。因此，我们在自定义事件时，也需要继承实现`ApplicationEvent`。

<img src="https://raw.githubusercontent.com/shuchang1011/images/main/img/SpringApplicationEvent%E7%B1%BB%E5%9B%BE.png" style="zoom:50%;" />

------

**实现步骤如下：**

```
实现方式主要有四种：
1.在初始化启动的时候，手动装载listener
2.自定义监听器通过Component组件的形式装载的Bean工厂
3.在resources目录下添加META-INF/spring.factories文件，并在org.springframework.context.ApplicationListener属性中添加自定义监听器
4.实现SmartApplicationListener，该接口继承了ApplicationListener，且添加了supportsEventType方法，可以对触发事件进行过滤
```

具体实现见代码模块

[springboot-listener](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-listener)

## 4）异步事件机制实现

在某些业务完成后，用户需要推送相关通知，这时，可以通过异步事件监听的机制来达到这一目的。而Spring提供的事件机制，默认是**同步**的。如果想要开启异步事件机制，Springboot也提供了相应的拓展方式，主要有以下两种：`配置applicationEventMulticaster的线程池`和`异步注解@Async`

### 1.配置applicationEventMulticaster的线程池

**实现原理**

Spring提供的事件机制，默认是**同步**的。如果想要使用异步事件监听，可以自己实现`ApplicationEventMulticaster`接口，并在Spring容器中注册id为`applicationEventMulticaster`的Bean ， 设置 executor 。

Spring会遍历所有的ApplicationListener， 如果 taskExecutor 不为空，就会开启异步线程执行。

![image-20211223163747848](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211223163747848.png)

------

**实现步骤**

```
1.构建自定义异步监听事件，继承ApplicationEvent父类
2.构建自定义异步监听器，实现SmartApplicationListener
3.通过@Component注解或者在spring.factories文件中配置当前AsyncListener，将该异步监听器注册到bean容器中
4.通过在初始化时配置名称为applicationEventMulticaster的bean的线程池，使得事件触发后，能多线程调用监听器的事件方法
```

具体实现见代码模块

[](springboot-listener-async)的method1模块

------

**源码解析**
Spring默认的事件广播器 `SimpleApplicationEventMulticaster#multicastEvent`
我们分析一下 `applicationContext.publishEvent(new AsyncEvent(Object source, String msg));` 最终会调用到

`org.springframework.context.event.SimpleApplicationEventMulticaster#multicastEvent(org.springframework.context.ApplicationEvent, org.springframework.core.ResolvableType)`
![image-20211224101917211](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224101917211.png)

-----------------------------------
在传播事件的时候，会去判断是否设置了线程池，从而决定是否采用异步处理事件

![image-20211224102237104](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224102237104.png)

![image-20211224102603272](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224102603272.png)

![image-20211224102536935](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224102536935.png)

所以说，只要在实例化SimpleApplicationEventMulticaster的时候 set属性值就可以了。

那么，问题来了，何时去设置这个属性呢？在spring容器进行refresh的阶段会触发`initApplicationEventMulticaster`操作，在这个方法内会去IOC容器上获取applicationEventMulticaster的bean对象，并重新赋值给上下文环境中的事件多播器对象。

![image-20211224103611680](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224103611680.png)

![image-20211224103642912](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224103642912.png)

综上所述，Spring会先从容器中找 bean name 为 `applicationEventMulticaster` 的 bean，so问题就简单了，我们只要自定义个 bean name 为 `applicationEventMulticaster` 的 bean，并给其属性 taskExecutor 赋上自定义的线程池即可，这个时候就能实现异步事件处理了 .

------



### 2.异步注解@Async

**实现原理**

主要是基于springboot自身提供的@Async注解。通过@EnableAsync开启异步执行功能后，springboot会使用默认设置的线程池去执行异步操作；然后，在需要进行异步处理的方法上声明@Async注解，也就是在我们的自定义监听器的事件触发函数`onApplicationEvent`上声明@Async；这样子，就能在事件触发时，异步调用`onApplicationEvent`的函数实现，而不影响主线程的使用了。

------

**实现步骤**

```
1.构建自定义异步监听事件，继承ApplicationEvent父类
2.构建自定义异步监听器，实现SmartApplicationListener
3.通过@Component注解或者在spring.factories文件中配置当前AsyncListener，将该异步监听器注册到bean容器中
4.使用springboot的异步注解@Async。使用前提：需在启动类上使用@EnableAsync开启异步执行的功能。然后将@Async修饰在需要异步执行的方法上，这里也就对应着listener中的onApplicationEvent方法。springboot使用的是默认的线程池，可以通过创建一个配置类，实现AsyncConfigurer的getExecutor方法，来生成指定的线程池
```