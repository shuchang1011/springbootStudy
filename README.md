# springbootStudy

# 项目介绍

该项目主要是针对springboot的启动过程进行分析，并结合springboot的一系列拓展实现相应的功能。

# 启动整体过程

![image-20211227163450830](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211227163450830.png)

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
2.自定义监听器通过Component组件的形式装载到Bean工厂
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

[springboot-listener-async](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-listener-async)的method1模块

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

具体实现见代码模块[springboot-listener-async](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-listener-async)的method2实现

------



## 二：构造容器环境

## 1）构造容器环境源码解析

在springboot完成了第一阶段监听器的获取和启动后，就会开始构建容器的运行环境了。

![image-20211224111354046](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224111354046.png)

`ConfigurableEnvironment environment = prepareEnvironment(listeners,applicationArguments);`
跟进去该方法：

```java
	private ConfigurableEnvironment prepareEnvironment(
			SpringApplicationRunListeners listeners,
			ApplicationArguments applicationArguments) {
		// Create and configure the environment
		//获取对应的ConfigurableEnvironment
		ConfigurableEnvironment environment = getOrCreateEnvironment();
		//配置
		configureEnvironment(environment, applicationArguments.getSourceArgs());
		//发布环境已准备事件，这是第二次发布事件
		listeners.environmentPrepared(environment);
		DefaultPropertiesPropertySource.moveToEnd(environment);
		Assert.state(!environment.containsProperty("spring.main.environment-prefix"),
				"Environment prefix cannot be set via properties.");
		bindToSpringApplication(environment);
		if (this.webApplicationType == WebApplicationType.NONE) {
			environment = new EnvironmentConverter(getClassLoader())
					.convertToStandardEnvironmentIfNecessary(environment);
		}
		ConfigurationPropertySources.attach(environment);
		return environment;
	}
```

来看一下`getOrCreateEnvironment()`方法，前面已经提到，`environment`已经被设置了`servlet`类型，所以这里创建的是环境对象是`StandardServletEnvironment`。

```java
private ConfigurableEnvironment getOrCreateEnvironment() {
   if (this.environment != null) {
      return this.environment;
   }
   switch (this.webApplicationType) {
   case SERVLET:
      return new ApplicationServletEnvironment();
   case REACTIVE:
      return new ApplicationReactiveWebEnvironment();
   default:
      return new ApplicationEnvironment();
   }
}
```

枚举类`WebApplicationType`是springBoot2新增的特性，主要针对spring5引入的reactive特性。枚举类型如下：

```java
public enum WebApplicationType {
	//不需要再web容器的环境下运行，普通项目
	NONE,
	//基于servlet的web项目
	SERVLET,
	//这个是spring5版本开始的新特性
	REACTIVE
}
```

`Environment`接口提供了4种实现方式，`StandardEnvironment`、`StandardServletEnvironment`和`MockEnvironment`、`StandardReactiveWebEnvironment`，分别代表普通程序、Web程序、测试程序的环境、响应式web环境，具体后面会详细讲解。
这里只需要知道在返回`return new StandardServletEnvironment();`对象的时候，会完成一系列初始化动作，主要就是将运行机器的系统变量和环境变量，加入到其父类`AbstractEnvironment`定义的对象`MutablePropertySources`中，`MutablePropertySources`对象中定义了一个属性集合：

```java
private final List<PropertySource<?>> propertySourceList = new CopyOnWriteArrayList<PropertySource<?>>();
```

执行到这里，系统变量和环境变量已经被载入到配置文件的集合中，接下来就行解析项目中的配置文件。

来看一下`listeners.environmentPrepared(environment);`，上面已经提到了，这里是第二次发布事件。什么事件呢？
顾名思义，系统环境初始化完成的事件。

![image-20211224113445174](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224113445174.png)发布事件的流程上面已经讲过了，这里不在赘述。来看一下根据事件类型获取到的监听器：

基于springboot2.4.0前的版本进行分析，之后的版本废弃了`ConfigFileApplicationListener`（为了兼容k8s的配置加载方式)；2.4.0前的版本优先加载的文件配置优先级更高，而2.4.0及以后的则是后加载的配置文件会覆盖前面加载的。详细见https://zhuanlan.zhihu.com/p/363354421

![img](https://img-blog.csdnimg.cn/20210716104720555.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzc2MjMwMw==,size_16,color_FFFFFF,t_70)

 可以看到获取到的监听器和第一次发布启动事件获取的监听器有几个是重复的，这也验证了监听器是可以多次获取，根据事件类型来区分具体处理逻辑。上面介绍日志监听器的时候已经提到。
主要来看一下`ConfigFileApplicationListener`，该监听器非常核心，主要用来处理项目配置。项目中的 properties 和yml文件都是其内部类所加载。具体来看一下：
首先方法执行入口：

![image-20211224170511353](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224170511353.png)

 首先还是会去读`spring.factories` 文件，`List<EnvironmentPostProcessor> postProcessors = this.loadPostProcessors();`获取的处理类有以上六种，这里`ConfigFileApplicationListener`同样也实现了`EnvironmentPostProcessor`接口，因此，它可以在spring的环境准备阶段，通过调用前置处理的方法加载对应的配置文件，并设置到Environment环境中来。

![image-20211224170739684](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224170739684.png)

可以看到ConfigFileApplicationListener触发postProcessEnvironment方法，加载对应的配置文件，并设置到Environment环境中去。

下面具体看看加载逻辑

![image-20211224171339495](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224171339495.png)

这里在进行load是，添加了过滤条件：`spring.profiles.active`和`spring.profiles.include`.

`spring.profiles.active`：在有多个命名规则遵循`application-${profile}.properties`的配置文件时，通过改参数来决定应用的配置文件

`spring.profiles.include`：使用该参数后，可以在引用了某个配置文件的基础上，在引用其他的配置文件，后引用的会覆盖前面引用的。

继续跟进load方法后，发现它会默认去以下位置去加载配置文件

`"classpath:/,classpath:/config/,file:./,file:./config/"`

![image-20211224171810542](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224171810542.png)

![image-20211224171833849](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224171833849.png)

![image-20211224171846655](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224171846655.png)

接下来仔细看load函数中的一个DocumentConsumer类型入参对象

![image-20211224172816706](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224172816706.png)

![image-20211224172911667](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211224172911667.png)

因此，在springboot2.4.0之前，还是通过ConfigFileApplicationListener加载配置文件的时候，配置优先级是按照文件加载顺序来实现的！

springboot2.4.0前后配置文件加载机制的具体差异详见：https://blog.csdn.net/weixin_42189048/article/details/111767740

------

## 2）自定义实现EnvironmentPostProcessor

在第一小节的分析中，可以看到`ConfigFileApplicationListener`通过实现了`ApplicationListener`接口，达到了在`prepareEnvironment`事件触发时去加载配置文件的效果;而在加载配置文件这块，主要是通过实现了`EnvironmentPostProcessor`接口来实现在设置环境时，将我们启动需要的配置文件中的属性加载到环境中来。因此，我们可以通过自定义的`EnvironmentPostProcessor`来加载启动过程中所必备的一些变量属性等。

**实现步骤如下：**

```
1.创建一个ProfileEnvironmentPostProcessor类，实现EnvironmentPostProcessor的postProcessEnvironment方法
2.在resource目录下创建META-INF/spring.factories文件，并为org.springframework.boot.env.EnvironmentPostProcessor添加一个后置处理器ProfileEnvironmentPostProcessor
```

具体实现见代码模块

[springboot-environment-postProcessor](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-environment-postprocessor)

[springboot-environment-postProcessor-profiles](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-environment-postprocessor-profiles)

其中，还在前面自定义异步监听事件的源码中，通过自定义实现的EnvironmentPostProcessor来加载配置的线程池参数，具体源码见

[springboot-listener-async](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-listener-async)的method2实现

## 3）自定义Banner

### 1.banner打印源码解析

在环境准备完成后，就会输出Banner

```java
Banner printedBanner = printBanner(environment);
```

那么，如何实现自定义banner输出呢？我们跟进源码，分析一下

![image-20211228104205790](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211228104205790.png)

在调用print方法打印时，会去获取Banner信息

![image-20211228104246345](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211228104246345.png)

![image-20211228153716751](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211228153716751.png)

继续跟进可以发现，springboot是通过environment中的`spring.banner.image.location`属性去加载banner的。因此，我们可以依赖springboot提供的这种方式，在application.properties中配置对应的属性路径，或者自定义EnvironmentPostProcessor去手动设置对应的属性，来达到加载自定义banner图片的效果。

![image-20211228160713322](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211228160713322.png)

`getTextBanner`也是同样的原理，只不过它是通过`spring.banner.location`属性去加载banner.txt文本文件

![image-20211228161040900](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211228161040900.png)

如果没有设置上述两个banner属性的话，springboot还会去加载自定义的Banner实现

![image-20211228161648129](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211228161648129.png)

![image-20211228161705611](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211228161705611.png)

![image-20211228161820687](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211228161820687.png)

这个banner是在构建SpringApplication对象时设置的，因此，我们可以入口类构建ApplicationContext上下文时，设置自定义的banner，这样就能达到自定义输出banner的效果。

![image-20211228161913836](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211228161913836.png)

------

### 2.自定义banner实现

根据上述分析得出，自定义banner主要有三种实现方式：

1.配置`spring.banner.image.location`，并上传banner图片

2.配置`spring.banner.location`，并上传名为`banner.txt`的文本

3.实现函数式接口`Banner`，并在入口类构建ApplicationContext上下文对象时，设置自定义Banner

具体实现见代码模块

[springboot-study-banner](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-study-banner)

## 三：**创建容器**

## 1）创建容器ApplicationContext源码解析

接下来，就是启动过程的第三步，上下文容器的创建

```java
context = createApplicationContext();
```

在web环境下，会实例化一个`AnnotationConfigServletWebServerApplicationContext`的上下文对象

![image-20211228102410984](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211228102410984.png)

实例化时，会去装载默认的bean的定义文件

![image-20211228102707387](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211228102707387.png)

![image-20211228102814134](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211228102814134.png)

例如，调用registerAnnotationConfigProcessors方法时，会去装载Bean名称为`internalConfigurationAnnotationProcessor`的`ConfigurationClassPostProcessor`配置类后置处理器等。（`ConfigurationClassPostProcessor`主要在后续准备容器时，加载@Configuration声明的配置类，或者通过@Import注解声明的需要导入的配置类）

![image-20211228103106710](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211228103106710.png)

------

## 四：报告错误信息

## 1）源码解析

![image-20211229105426150](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211229105426150.png)

该阶段主要是由springboot通过spring.factories去加载获取`SpringBootExceptionReporter`的实现类`FailureAnalyzers`，在`FailureAnalyzers`会持有多个Analyzer对异常进行分析，最后通过`FailureAnalysisReporter`输出相应的异常信息

```yaml
# Error Reporters
org.springframework.boot.SpringBootExceptionReporter=\
org.springframework.boot.diagnostics.FailureAnalyzers

# Failure Analyzers
org.springframework.boot.diagnostics.FailureAnalyzer=\
org.springframework.boot.diagnostics.analyzer.BeanCurrentlyInCreationFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.BeanDefinitionOverrideFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.BeanNotOfRequiredTypeFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.BindFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.BindValidationFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.UnboundConfigurationPropertyFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.ConnectorStartFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.NoSuchMethodFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.NoUniqueBeanDefinitionFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.PortInUseFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.ValidationExceptionFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.InvalidConfigurationPropertyNameFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.InvalidConfigurationPropertyValueFailureAnalyzer

# FailureAnalysisReporters
org.springframework.boot.diagnostics.FailureAnalysisReporter=\
org.springframework.boot.diagnostics.LoggingFailureAnalysisReporter
```

通过这个`FailureAnalyzers`来实现在项目启动失败之后，打印log的效果

可以看到，在启动过程中，springboot会捕获异常，并调用`handleRunFailure`，打印启动过程中的异常信息

![image-20211229102058220](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211229102058220.png)

```java
private void handleRunFailure(ConfigurableApplicationContext context,
		Throwable exception,
		Collection<SpringBootExceptionReporter> exceptionReporters,
		SpringApplicationRunListeners listeners) {
	try {
		try {
			// 1. ExitCodeGenerators 根据异常获取是正常不是异常退出
			handleExitCode(context, exception);
			if (listeners != null) {
				listeners.failed(context, exception);
			}
		} finally {
			// 2. SpringBootExceptionReporter 处理异常报告
			reportFailure(exceptionReporters, exception);
			if (context != null) {
				context.close();
			}
		}
	} catch (Exception ex) {
		logger.warn("Unable to close ApplicationContext", ex);
	}
	// 3. 重新报出异常，由 SpringBootExceptionHandler 处理
	ReflectionUtils.rethrowRuntimeException(exception);
}
```

handleRunFailure 中主要依赖了三个组件完成异常的处理：

- `SpringBootExceptionReporter` 生成错误报告并处理，主要是用于输出日志。
- `SpringBootExceptionHandler` 实现了 Thread#UncaughtExceptionHandler 接口，可以在线程异常关闭的时候进行回调。主要用于退出程序 System.exit(xxx)
- `SpringApplicationRunListeners` Spring Boot 事件机制

------

首先，我们分析一些`handleExitCode`方法

`handleExitCode`会 根据异常的类型决定如何退出程序，并将 exitCode(0 或 1) 退出码注册到 `SpringBootExceptionHandler `上

```java
private void handleExitCode(ConfigurableApplicationContext context, Throwable exception) {
   // 根据异常判断是正常退出还是异常退出
   int exitCode = getExitCodeFromException(context, exception);
   if (exitCode != 0) {
      if (context != null) {
         context.publishEvent(new ExitCodeEvent(context, exitCode));
      }
      SpringBootExceptionHandler handler = getSpringBootExceptionHandler();
      if (handler != null) {
          // 正常退出或异常退出，System.exit(exitCode) 用
         handler.registerExitCode(exitCode);
      }
   }
}
```

`getExitCodeFromException` 根据异常判断是正常退出还是异常退出，委托给了 `ExitCodeGenerators`，最后将退出码注册到 `SpringBootExceptionHandler `上。

然后，由`SpringbootExceptionHandler`来处理程序的退出

接下来，会通知已装载的监听器触发应用启动失败的事件。

![image-20211229110519498](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211229110519498.png)

紧接着，就会通过`reportFailure`将异常委托给`SpringBootExceptionReporter `进行处理

![image-20211229145023821](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211229145023821.png)

首先会遍历`SpringBootExceptionReporter `实现类，对异常信息进行分析，然后打印异常日志信息

![image-20211229150322649](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211229150322649.png)

------

## 2）自定义实现FailureAnalyzer（拦截启动异常）

springboot在启动过程中处理异常时，最终会执行`reportException`方法，遍历analyzers对异常信息进行分析，然后交由`SpringBootExceptionReporter`输出处理

```java
public boolean reportException(Throwable failure) {
   FailureAnalysis analysis = analyze(failure, this.analyzers);
   return report(analysis, this.classLoader);
}
```

因此，我们只需要自定义实现analyzer，针对不同的异常进行相应的处理即可。

**实现步骤**

```java
1.自定义FailureAnalyzer，实现AbstractFailureAnalyzer
2.在springboot启动执行完第四阶段后，抛出自定义异常（在此阶段前，还尚未装载自定义的Analyzer）；这里我们声明一个配置类，加载不存在的配置项，使其抛出IllegalArgumentException
注意：该analyzer只能拦截启动过程中出现的异常
```

具体实现见代码模块

[springboot-exception-analyzer](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-exception-failureAnalyzer)

![image-20211230153417647](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20211230153417647.png)

------

## 五：**准备容器**

这一步主要是在容器刷新之前的准备动作。其主要做了两件事：`应用初始化方法`、`注册入口类的定义信息`

```java
prepareContext(context, environment, listeners, applicationArguments, printedBanner);
```

首先，看一下整个处理流程

```java
private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment,
      SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
    //设置容器环境，包括各种变量
   context.setEnvironment(environment);
    //执行容器后置处理
   postProcessApplicationContext(context);
    //执行容器中的ApplicationContextInitializer（包括 spring.factories和自定义的实例）
   applyInitializers(context);
    //发送容器已经准备好的事件，通知各监听器
   listeners.contextPrepared(context);
    //打印log
   if (this.logStartupInfo) {
      logStartupInfo(context.getParent() == null);
      logStartupProfileInfo(context);
   }
   // Add boot specific singleton beans
    //注册启动参数bean，这里将容器指定的参数封装成bean，注入容器
   ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
   beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
    //设置banner
   if (printedBanner != null) {
      beanFactory.registerSingleton("springBootBanner", printedBanner);
   }
   if (beanFactory instanceof DefaultListableBeanFactory) {
      ((DefaultListableBeanFactory) beanFactory)
            .setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
   }
   if (this.lazyInitialization) {
      context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
   }
   // Load the sources
    //获取我们的启动类指定的参数，可以是多个
   Set<Object> sources = getAllSources();
   Assert.notEmpty(sources, "Sources must not be empty");
    //加载我们的启动类，将启动类注入容器
   load(context, sources.toArray(new Object[0]));
    //发布容器已加载事件。
   listeners.contextLoaded(context);
}
```

这里我们主要看一下几个核心的处理：

### **1）容器的后置处理**

这里主要做了两件事：

判断是否存在自定义的`beanNameGenerator`和`resouceLoader`,并将其注册到我们的bean工厂中。默认情况下，这二者都是空的，不会执行任何逻辑。

```java
protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
   if (this.beanNameGenerator != null) {
      context.getBeanFactory().registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR,
            this.beanNameGenerator);
   }
   if (this.resourceLoader != null) {
      if (context instanceof GenericApplicationContext) {
         ((GenericApplicationContext) context).setResourceLoader(this.resourceLoader);
      }
      if (context instanceof DefaultResourceLoader) {
         ((DefaultResourceLoader) context).setClassLoader(this.resourceLoader.getClassLoader());
      }
   }
   if (this.addConversionService) {
      context.getBeanFactory().setConversionService(ApplicationConversionService.getSharedInstance());
   }
}
```

这里提到了两个模块：`beanNameGenerator`和`resouceLoader`，我们将对他们分别进行讲解。

#### BeanNameGenerator

官方解释：

```
When a component is autodetected as part of the scanning process, its bean name is generated by the `BeanNameGenerator` strategy known to that scanner. By default, any Spring stereotype annotation (`@Component`, `@Repository`, `@Service`, and `@Controller`) that contains a name `value` thereby provides that name to the corresponding bean definition.

If such an annotation contains no name `value` or for any other detected component (such as those discovered by custom filters), the default bean name generator returns the uncapitalized non-qualified class name. For example, if the following component classes were detected, the names would be `myMovieLister` and `movieFinderImpl`:
```

```
当一个组件作为扫描过程的一部分被自动检测时，它的 bean 名称由该`BeanNameGenerator`扫描器已知的策略生成。默认情况下，任何包含名称的Spring 构造型注释（`@Component`、`@Repository`、`@Service`和 `@Controller`）`value`从而将该名称提供给相应的 bean 定义。

如果这样的注释不包含名称`value`或任何其他检测到的组件（例如自定义过滤器发现的组件），则默认 bean 名称生成器返回未大写的非限定类名称。例如，如果检测到以下组件类，名称将是`myMovieLister`和`movieFinderImpl`：
```

```java
@Service("myMovieLister")
public class SimpleMovieLister {
    // ...
}
```

```java
@Repository
public class MovieFinderImpl implements MovieFinder {
    // ...
}
```

**综上所述，对于基于@Component(包含@Controller,@Service,@Repository)组件声明的类，springboot会用默认的`BeanNameGenerator`将bean名由类全限定名转化为小写开头的非限定类名(只有类名，不包含包的路径)**

如果不想依赖默认的 bean 命名策略，可以提供自定义 bean 命名策略。首先，实现 [`BeanNameGenerator`](https://docs.spring.io/spring-framework/docs/5.3.14/javadoc-api/org/springframework/beans/factory/support/BeanNameGenerator.html) 接口，并确保包含一个默认的无参数构造函数。然后，在配置扫描器时提供完全限定的类名，如以下示例注释和 bean 定义所示。

 **如果由于多个自动检测到的组件具有相同的非限定类名（即具有相同名称但驻留在不同包中的类）而遇到命名冲突，您可能需要配置一个`BeanNameGenerator`默认为生成的完全限定类名豆名。从 Spring Framework 5.2.3 开始， `FullyQualifiedAnnotationBeanNameGenerator`位于包 `org.springframework.context.annotation`中的可用于此类目的。**

```java
@Configuration
@ComponentScan(basePackages = "org.example", nameGenerator = MyNameGenerator.class)
public class AppConfig {
    // ...
}
```

```java
<beans>
    <context:component-scan base-package="org.example"
        name-generator="org.example.MyNameGenerator" />
</beans>
```

#### 1.1自定义BeanNameGenerator实现

**为啥需要自定义BeanNameGenerator？**

springboot默认提供的`BeanNameGenerator`采取的命名策略会**将bean名由类全限定名转化为小写开头的非限定类名(只有类名，不包含包的路径)**。因此，如果在不同包路径下，存在同名的类时，采用默认的命名策略就会导致命名冲突。针对这一问题，spring也提供了`FullyQualifiedAnnotationBeanNameGenerator`来生成全限定名的bean装载。

注意事项:

通过自定义BeanNameGenerator来实现自定义命名策略时，需要注意，自定义的命名策略需要和默认的命名策略生成的Bean名称区分开，否则同样会出现命名冲突的问题！！！

例如：

针对User类

```java
package cn.com.shuchang.springboot.study.pojo;

@Component
public class User {

    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}
```

扫描时，springboot默认装载的bean名称为user，而通过自定义策略装载的bean名称为它的全限定名

![image-20220104144332791](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220104144332791.png)

下面，将根据是否存在同名类两种情况提供不同的实现

**实现步骤**

无同名类

```java
1.自定义CustomBeanNameGenerator实现接口BeanNameGenerator,可以springboot默认实现DefaultBeanNameGenerator
2.使用配置类结合@ComponentScan注解，指定BeanNameGererator为CustomBeanNameGenerator
3.由于入口类注解@SpringBootApplication种包含了@ComponentScan注解，因此，启动时，会基于默认的BeanNameGenerator去装载对应命名的Bean definition。同时，加载到配置类时，又会触发一次扫描，基于自定义的BeanNameGenerator再次装载一个符合自定义命名策略的Bean Definition。因此，自定义命名策略需要和默认的命名策略区分开，否则会导致命名冲突的问题出现
```

具体实现见代码(其中在注解扫描过程中添加了自定义类型过滤器)

[springboot-beanNameGenerator-case1](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-beanNameGenerator-case1)

------

存在同名类

```
1.自定义CustomBeanNameGenerator实现接口BeanNameGenerator,可以springboot默认实现DefaultBeanNameGenerator
2.使用配置类结合@ComponentScan注解，指定BeanNameGererator为CustomBeanNameGenerator
3.由于入口类注解@SpringBootApplication种包含了@ComponentScan注解，因此，启动时，回基于默认的BeanNameGenerator去装载对应命名的Bean definition。同时，加载到配置类时，又会触发一次扫描，基于自定义的BeanNameGenerator再次装载一个符合自定义命名策略的Bean Definition。因此，自定义命名策略需要和默认的命名策略区分开，否则会导致命名冲突的问题出现
4.如果存在不同包路径下有多个同名类，使用默认的BeanNameGenerator加载会有命名冲突的问题，这是可以使用自定义BeanNameGenerator实现全限定名的装载，或者使用spring提供的FullyQualifiedAnnotationBeanNameGenerator
```

具体实现见代码

[springboot-beanNameGenerator-case2](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-beanNameGenerator-case2)

------

### 2）初始化ApplicationContext

在执行完`postProcessApplicationContext`后，就会开始调用`ApplicationContApplicatoiextInitializer`，在容器刷新（执行refresh）前，注入一些属性。

那么，`ApplicationContApplicatoiextInitializer`是在何时装载好的呢？

在springboot启动构建`SpringApplication`对象时，就已经从`spring.factories`文件中解析出默认的Initializer了

```java
public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
   this.resourceLoader = resourceLoader;
   Assert.notNull(primarySources, "PrimarySources must not be null");
   this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
   this.webApplicationType = WebApplicationType.deduceFromClasspath();
   //加载spring.factories中的ApplicationContextInitializer实现
   setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
   setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
   this.mainApplicationClass = deduceMainApplicationClass();
}
```

接下来，就会开始遍历装载的initializers，执行回调函数initialize

```java
protected void applyInitializers(ConfigurableApplicationContext context) {
   for (ApplicationContextInitializer initializer : getInitializers()) {
      Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(initializer.getClass(),
            ApplicationContextInitializer.class);
      Assert.isInstanceOf(requiredType, context, "Unable to call initializer.");
      initializer.initialize(context);
   }
}
```

下面，介绍接种springboot默认提供的Initializer

**DelegatingApplicationContextInitializer**

```
使用环境属性 context.initializer.classes 指定的初始化器(initializers)进行初始化工作，如果没有指定则什么都不做。
通过它使得我们可以把自定义实现类配置在 application.properties 里成为了可能。
```

**ContextIdApplicationContextInitializer**

```
设置Spring应用上下文的ID,会参照环境属性。至于Id设置为什么值，将会参考环境属性：
* spring.application.name
* vcap.application.name
* spring.config.name
* spring.application.index
* vcap.application.instance_index
如果这些属性都没有，ID 使用 application。
```

**ConfigurationWarningsApplicationContextInitializer**

```
对于一般配置错误在日志中作出警告
```

**ServerPortInfoApplicationContextInitializer**

```
 将内置 servlet容器实际使用的监听端口写入到 Environment 环境属性中。这样属性 local.server.port 就可以直接通过 @Value 注入到测试中，或者通过环境属性 Environment 获取。
```

**SharedMetadataReaderFactoryContextInitializer**

```
创建一个 SpringBoot和ConfigurationClassPostProcessor 共用的 CachingMetadataReaderFactory对象。实现类为：ConcurrentReferenceCachingMetadataReaderFactory
```

**ConditionEvaluationReportLoggingListener**

```
将 ConditionEvaluationReport写入日志。
```

#### 2.1 自定义ApplicationContextInitializer实现

自定义ApplicationContextInitializer可以在容器刷新之前，执行回调函数，往spring的容器中注入属性

这里主要有两种实现方式，通过自定义`ApplicationContextInitializer`实现`ApplicationContextInitializer`接口。然后，有两种装载方式：

1.在spring.factories中配置`org.springframework.context.ApplicationContextInitializer`的属性为`自定义。ApplicationContextInitializer`的全限定名；

2.通过在配置文件`application.properties`中通过`context.initializer.classes`来指定`自定义ApplicationContextInitializer`

上述方法中，通过方法2指定的`自定义ApplicationContextInitializer`优先级是高于方法1的，也就是说如果同时定义两个`ApplicationContextInitializer`，方法2加载的`ApplicationContextInitializer`会优先调用`initialize`方法

为什么会出现上述现象呢？

这是因为在springboot初始化`SpringApplication`时，设置了几个默认的Initializer，其中就包含了**DelegatingApplicationContextInitializer**

这个类的作用主要是使用环境属性`context.initializer.classes`指定的初始化器(initializers)进行初始化工作，且其实现了Ordered接口，默认设置order为0，调用优先级最高

```java
	private static final String PROPERTY_NAME = "context.initializer.classes";

	//实现了Ordered接口，且默认设置order为0，优先级最高
	private int order = 0;

	@Override
	public void initialize(ConfigurableApplicationContext context) {
		ConfigurableEnvironment environment = context.getEnvironment();
        //获取context.initializer.classes指定的Initializers
		List<Class<?>> initializerClasses = getInitializerClasses(environment);
		if (!initializerClasses.isEmpty()) {
            //调用对应Initializer的initialize
			applyInitializerClasses(context, initializerClasses);
		}
	}

	private List<Class<?>> getInitializerClasses(ConfigurableEnvironment env) {
		String classNames = env.getProperty(PROPERTY_NAME);
		List<Class<?>> classes = new ArrayList<>();
		if (StringUtils.hasLength(classNames)) {
			for (String className : StringUtils.tokenizeToStringArray(classNames, ",")) {
				classes.add(getInitializerClass(className));
			}
		}
		return classes;
	}
```

而我们通过spring.factories指定的自定义Initializers的调用优先级没有`DelegatingApplicationContextInitializer`高，因此，通过配置文件`application.properties`声明的Initializers优先调用于通过spring.factories配置的Initializers



**实现步骤**

```java
1.通过自定义`ApplicationContextInitializer`实现`ApplicationContextInitializer`接口
2.通过配置spring.factories指定org.springframework.context.ApplicationContextInitializer为自定义ApplicationContextInitializer；或者通过在配置文件application.yaml中配置contex.initializer.classes属性为自定义ApplicationContextInitializer （后者调用优先级高于前者）
```

具体代码实现见模块

[springboot-applicationContext-initializer](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-applicationContext-initializer)

------

### 3）load加载启动指定类(重点)

在调用完成ApplicationContextInitializers设置容器中的属性后，就会执行下列操作：

```java
// Add boot specific singleton beans
ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
//注册启动入参
beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
if (printedBanner != null) {
//注册banner
beanFactory.registerSingleton("springBootBanner", printedBanner);
}
if (beanFactory instanceof DefaultListableBeanFactory) {
//设置同名bean时，是否允许后者覆盖前者
//可以通过applicationContext.setAllowBeanDefinitionOverriding来修改属性
((DefaultListableBeanFactory) beanFactory)
.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
}
if (this.lazyInitialization) {
//设置是否开启懒加载模式来实例化bean,可以减少启动耗时,但是会增加第一次请求的响应延迟
//开启方式：配置spring.main.lazy-initialization=true
context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
}
```

紧接着，就来到了准备容器阶段的重点：加载启动指定类

```java
// Load the sources
Set<Object> sources = getAllSources();
Assert.notEmpty(sources, "Sources must not be empty");
load(context, sources.toArray(new Object[0]));
listeners.contextLoaded(context);
```

首先通过`getAllSources()`中拿到了我们的启动类。　

```java
Set<Object> sources = getAllSources();

// 装载启动类到加载资源中，启动类为primarySources
public Set<Object> getAllSources() {
    Set<Object> allSources = new LinkedHashSet<>();
    if (!CollectionUtils.isEmpty(this.primarySources)) {
        allSources.addAll(this.primarySources);
    }
    if (!CollectionUtils.isEmpty(this.sources)) {
        allSources.addAll(this.sources);
    }
    return Collections.unmodifiableSet(allSources);
}

// 入口类中调用SpringApplication.run(class,args)时，会传入入口类，然后将入口类设置为primarySources
public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
    return new SpringApplication(primarySources).run(args);
}

public SpringApplication(Class<?>... primarySources) {
    this(null, primarySources);
}

public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
    this.resourceLoader = resourceLoader;
    Assert.notNull(primarySources, "PrimarySources must not be null");
    this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
    this.webApplicationType = WebApplicationType.deduceFromClasspath();
    setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
    setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
    this.mainApplicationClass = deduceMainApplicationClass();
}
```

然后，会通过调用`load()`来加载指定启动类

```java
load(context, sources.toArray(new Object[0]));
```

**这里会将我们的启动类加载spring容器`beanDefinitionMap`中，为后续springBoot 自动化配置奠定基础，`springBoot`为我们提供的各种注解配置也与此有关。**

```java
/**
 * Load beans into the application context.
 * @param context the context to load beans into
 * @param sources the sources to load
 */
protected void load(ApplicationContext context, Object[] sources) {
   if (logger.isDebugEnabled()) {
      logger.debug("Loading source " + StringUtils.arrayToCommaDelimitedString(sources));
   }
   // 创建 BeanDefinitionLoader 
   BeanDefinitionLoader loader = createBeanDefinitionLoader(getBeanDefinitionRegistry(context), sources);
   if (this.beanNameGenerator != null) {
      loader.setBeanNameGenerator(this.beanNameGenerator);
   }
   if (this.resourceLoader != null) {
      loader.setResourceLoader(this.resourceLoader);
   }
   if (this.environment != null) {
      loader.setEnvironment(this.environment);
   }
   loader.load();
}
```

#### 3.1 获取BeanDefinitionLoader

这里，我们首先会创建一个`BeanDefinitionLoader`来加载启动类的bean的定义文件

##### 3.1.1 getBeanDefinitionRegistry(context)

这里，会将我们的`applicationContext`上下文对象强制转换成`BeanDifinitionRegistry`

```java
/**
* Get the bean definition registry.
* @param context the application context
* @return the BeanDefinitionRegistry if it can be determined
*/
private BeanDefinitionRegistry getBeanDefinitionRegistry(ApplicationContext context) {
    if (context instanceof BeanDefinitionRegistry) {
        return (BeanDefinitionRegistry) context;
    }
    if (context instanceof AbstractApplicationContext) {
        return (BeanDefinitionRegistry) ((AbstractApplicationContext) context).getBeanFactory();
    }
    throw new IllegalStateException("Could not locate BeanDefinitionRegistry");
}
```

我们在第三阶段创建上下文对象时，可以看到其实例化了一个`AnnotationConfigServletWebServerApplicationContext`对象，而它正好实现了`BeanDefinitionRegistry`接口

![image-20220112164750803](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220112164750803.png)

![image-20220112164916792](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220112164916792.png)

因此，springboot可以直接通过context对象获取到对应的`BeanDefinitioinRegistry`。而在`BeanDefinitionRegistry`定义了很重要的方法`registerBeanDefinition()`，该方法将`BeanDefinition`注册进`DefaultListableBeanFactory`容器的`beanDefinitionMap`中

![image-20220112165719830](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220112165719830.png)

##### 3.1.2 createBeanDefinitionLoader(getBeanDefinitionRegistry(context), sources)

紧接着就会通过获取到的`BeanDefinitionRegistry`构建`BeanDefinitionLoader`

```java
protected BeanDefinitionLoader createBeanDefinitionLoader(BeanDefinitionRegistry registry, Object[] sources) {
    return new BeanDefinitionLoader(registry, sources);
}
```

```java
/**
* Create a new {@link BeanDefinitionLoader} that will load beans into the specified
* {@link BeanDefinitionRegistry}.
* @param registry the bean definition registry that will contain the loaded beans
* @param sources the bean sources
*/
BeanDefinitionLoader(BeanDefinitionRegistry registry, Object... sources) {
    Assert.notNull(registry, "Registry must not be null");
    Assert.notEmpty(sources, "Sources must not be empty");
    this.sources = sources;
    this.annotatedReader = new AnnotatedBeanDefinitionReader(registry);
    this.xmlReader = new XmlBeanDefinitionReader(registry);
    if (isGroovyPresent()) {
        this.groovyReader = new GroovyBeanDefinitionReader(registry);
    }
    this.scanner = new ClassPathBeanDefinitionScanner(registry);
    this.scanner.addExcludeFilter(new ClassExcludeFilter(sources));
}
```

#### 3.2  重点步骤loader.load()

跟进load()方法

```java
private int load(Object source) {
    Assert.notNull(source, "Source must not be null");
    // 从Class加载
    if (source instanceof Class<?>) {
        return load((Class<?>) source);
    }
    // 从Resource加载
    if (source instanceof Resource) {
        return load((Resource) source);
    }
    // 从Package加载
    if (source instanceof Package) {
        return load((Package) source);
    }
    // 从CharSequence加载
    if (source instanceof CharSequence) {
        return load((CharSequence) source);
    }
    throw new IllegalArgumentException("Invalid source type " + source.getClass());
}
```

紧接着，跟进`load((Class<?>) source)`来到了`doRegisterBean`

```java
private <T> void doRegisterBean(Class<T> beanClass, @Nullable String name,
      @Nullable Class<? extends Annotation>[] qualifiers, @Nullable Supplier<T> supplier,
      @Nullable BeanDefinitionCustomizer[] customizers) {

   //将指定的类 封装为AnnotatedGenericBeanDefinition
   AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
   if (this.conditionEvaluator.shouldSkip(abd.getMetadata())) {
      return;
   }
   
    // 获取该类的 scope 属性
   abd.setInstanceSupplier(supplier);
   ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
   abd.setScope(scopeMetadata.getScopeName());
   String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));

   AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
   if (qualifiers != null) {
      for (Class<? extends Annotation> qualifier : qualifiers) {
         if (Primary.class == qualifier) {
            abd.setPrimary(true);
         }
         else if (Lazy.class == qualifier) {
            abd.setLazyInit(true);
         }
         else {
            abd.addQualifier(new AutowireCandidateQualifier(qualifier));
         }
      }
   }
   if (customizers != null) {
      for (BeanDefinitionCustomizer customizer : customizers) {
         customizer.customize(abd);
      }
   }
   // 将该BeanDefinition注册到IoC容器的beanDefinitionMap中
   BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
   definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
   BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
}
```

在该方法中将主类封装成`AnnotatedGenericBeanDefinition`

`BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);`方法将BeanDefinition注册进beanDefinitionMap

```java
public static void registerBeanDefinition(
      BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
      throws BeanDefinitionStoreException {

   // Register bean definition under primary name.
   // primary name 其实就是id吧
   String beanName = definitionHolder.getBeanName();
   registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());

   // Register aliases for bean name, if any.
   // 然后就是注册别名
   String[] aliases = definitionHolder.getAliases();
   if (aliases != null) {
      for (String alias : aliases) {
         registry.registerAlias(beanName, alias);
      }
   }
}
```

继续跟进`registerBeanDefinition`，这里会来到我们Ioc容器的具体实现类`DefaultListableBeanFactory`

```java
@Override
public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
      throws BeanDefinitionStoreException {

   Assert.hasText(beanName, "Bean name must not be empty");
   Assert.notNull(beanDefinition, "BeanDefinition must not be null");

   if (beanDefinition instanceof AbstractBeanDefinition) {
      try {
          // 最后一次校验了
          // 对bean的Overrides进行校验
         ((AbstractBeanDefinition) beanDefinition).validate();
      }
      catch (BeanDefinitionValidationException ex) {
         throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
               "Validation of bean definition failed", ex);
      }
   }
   // 判断是否存在重复名字的bean，之后看允不允许override
   // 以前使用synchronized实现互斥访问，现在采用ConcurrentHashMap
   BeanDefinition existingDefinition = this.beanDefinitionMap.get(beanName);
   if (existingDefinition != null) {
      //如果该类不允许 Overriding 直接抛出异常
      if (!isAllowBeanDefinitionOverriding()) {
         throw new BeanDefinitionOverrideException(beanName, beanDefinition, existingDefinition);
      }
      else if (existingDefinition.getRole() < beanDefinition.getRole()) {
         // e.g. was ROLE_APPLICATION, now overriding with ROLE_SUPPORT or ROLE_INFRASTRUCTURE
         if (logger.isInfoEnabled()) {
            logger.info("Overriding user-defined bean definition for bean '" + beanName +
                  "' with a framework-generated bean definition: replacing [" +
                  existingDefinition + "] with [" + beanDefinition + "]");
         }
      }
      else if (!beanDefinition.equals(existingDefinition)) {
         if (logger.isDebugEnabled()) {
            logger.debug("Overriding bean definition for bean '" + beanName +
                  "' with a different definition: replacing [" + existingDefinition +
                  "] with [" + beanDefinition + "]");
         }
      }
      else {
         if (logger.isTraceEnabled()) {
            logger.trace("Overriding bean definition for bean '" + beanName +
                  "' with an equivalent definition: replacing [" + existingDefinition +
                  "] with [" + beanDefinition + "]");
         }
      }
      //注册启动类的BeanDefinition到beanDefinitionMap中
      this.beanDefinitionMap.put(beanName, beanDefinition);
   }
   else {
      if (hasBeanCreationStarted()) {
         // Cannot modify startup-time collection elements anymore (for stable iteration)
         // 运行阶段需要加锁注册进beanDefinitionMap
         synchronized (this.beanDefinitionMap) {
            this.beanDefinitionMap.put(beanName, beanDefinition);
            List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames.size() + 1);
            updatedDefinitions.addAll(this.beanDefinitionNames);
            updatedDefinitions.add(beanName);
            this.beanDefinitionNames = updatedDefinitions;
            removeManualSingletonName(beanName);
         }
      }
      else {
         // Still in startup registration phase
         // 如果仍处于启动注册阶段，直接注册进beanDefinitionMap
         this.beanDefinitionMap.put(beanName, beanDefinition);
         this.beanDefinitionNames.add(beanName);
         removeManualSingletonName(beanName);
      }
      this.frozenBeanDefinitionNames = null;
   }

   if (existingDefinition != null || containsSingleton(beanName)) {
      resetBeanDefinition(beanName);
   }
}
```

仔细看这个方法`registerBeanDefinition()`，首先会检查是否已经存在，如果存在并且不允许被覆盖则直接抛出异常。不存在的话就直接注册进beanDefinitionMap中。

接下来我们断点调试，跳过准备容器阶段，可以看到启动类的`BeanDefinition`已经成功加载到Ioc容器的`BeanDefinitionMap`中了

![image-20220114152931219](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220114152931219.png)

------

## 六： 刷新容器

接下来，便是springboot启动过程中最为核心的步骤：`refreshContext(context)`

这里我们直接跳到关键步骤`refresh()`

```java
public void refresh() throws BeansException, IllegalStateException {
   synchronized (this.startupShutdownMonitor) {
      // Prepare this context for refreshing.
      // 准备刷新
      prepareRefresh();

      // Tell the subclass to refresh the internal bean factory.
      // 获取 beanFactory
      // 如果是 AbstractRefreshableApplicationContext 子类，在这里会 refresh bean factory 并加载 BeanDefinition
      // 加载 BeanDefinition 由子类的 loadBeanDefinitions 实现
      ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

      // Prepare the bean factory for use in this context.
      prepareBeanFactory(beanFactory);

      try {
         // Allows post-processing of the bean factory in context subclasses.
         postProcessBeanFactory(beanFactory);

         // Invoke factory processors registered as beans in the context.
         invokeBeanFactoryPostProcessors(beanFactory);

         // Register bean processors that intercept bean creation.
         registerBeanPostProcessors(beanFactory);

         // Initialize message source for this context.
         initMessageSource();

         // Initialize event multicaster for this context.
         initApplicationEventMulticaster();

         // Initialize other special beans in specific context subclasses.
         onRefresh();

         // Check for listener beans and register them.
         registerListeners();

         // Instantiate all remaining (non-lazy-init) singletons.
         finishBeanFactoryInitialization(beanFactory);

         // Last step: publish corresponding event.
         finishRefresh();
      }

      catch (BeansException ex) {
         if (logger.isWarnEnabled()) {
            logger.warn("Exception encountered during context initialization - " +
                  "cancelling refresh attempt: " + ex);
         }

         // Destroy already created singletons to avoid dangling resources.
         destroyBeans();

         // Reset 'active' flag.
         cancelRefresh(ex);

         // Propagate exception to caller.
         throw ex;
      }

      finally {
         // Reset common introspection caches in Spring's core, since we
         // might not ever need metadata for singleton beans anymore...
         resetCommonCaches();
      }
   }
}
```

------

### **1）准备刷新prepareRefresh()**

```java
{
    //系统启动时间
    this.startupDate = System.currentTimeMillis();
    //是否关闭标识，false
    this.closed.set(false);
    //是否活跃标识，true
    this.active.set(true);
    if (this.logger.isDebugEnabled()) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Refreshing " + this);
        } else {
            this.logger.debug("Refreshing " + this.getDisplayName());
        }
    }

    // 调用子类重写后的方法替换servlet相关属性源，即子类自定义个性化的属性设置方法
    this.initPropertySources();
    // 这里是验证由ConfigurablePropertyResolver#setRequiredProperties()方法指定的属性，解析为非空值，如果没有设置的话这个方法就不会执行什么操作。
    this.getEnvironment().validateRequiredProperties();
    if (this.earlyApplicationListeners == null) {
        // Store pre-refresh ApplicationListeners
        this.earlyApplicationListeners = new LinkedHashSet(this.applicationListeners);
    } else {
        this.applicationListeners.clear();
        this.applicationListeners.addAll(this.earlyApplicationListeners);
    }

    this.earlyApplicationEvents = new LinkedHashSet();
}

```

在这一步骤中，主要的是`initPropertySources()`和`getEnvironment.validateRequireProperties()`

其中`initPropertySources()`是springboot提供的一个扩展点，它可以方便用户继承AbstractApplicationContext的子类并实现该方法来达到自定义设置属性到Environment中

例如，在web应用中，它会去调用`GenericWebApplicationContext`的相应方法

```java
protected void initPropertySources() {
   ConfigurableEnvironment env = getEnvironment();
   if (env instanceof ConfigurableWebEnvironment) {
      ((ConfigurableWebEnvironment) env).initPropertySources(this.servletContext, null);
   }
}
```

#### 1.1 扩展initPropertySources()实现

作用：扩展initPropertySources()主要是为了在准备刷新容器时，往Environment环境中添加一些自定义的属性；相对于在第二阶段-准备环境过程中，通过实现EnvironmentPostProcessors接口来说，二者作用一致，但是执行的时机不一样。

实现步骤：

```java
1.因为我们是以web应用的方式启动springboot，因此，可以继承其实现类AnnotationConfigServletWebApplicationContext，并重写重写initPropertySources
2.在initPropertySources方法中，往环境Environment中设置必要属性，启动测试是否生效
```

具体实现见代码模块

[springboot-context-initPropertySouces](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-context-initPropertySources)

------

### 2）通知子类刷新内部bean工厂obtainFreshBeanFactory()

在完成`prepareRefresh`准备刷新阶段后，就会获取`DefaultListableBeanFactory`

```java
// 获取 beanFactory
// 如果是 AbstractRefreshableApplicationContext 子类，在这里会 refresh bean factory 并加载 BeanDefinition
// 加载 BeanDefinition 由子类的 loadBeanDefinitions 实现
ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
```

深入`obtainFreshBeanFactory()`，可以看到其主要通过`refreshBeanFactory()`来刷新Beanfactory

```java
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
   refreshBeanFactory();
   return getBeanFactory();
}
```

而`refreshBeanFactory()`主要由两个类实现`AbstractRefreshableApplicationContext`和`GenericApplicationContext`

![image-20220117154202872](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220117154202872.png)

二者都是实现了接口`ApplicationContext`

![image-20220117155004753](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220117155004753.png)

大部分场景所使用的 application context 都由` GenericApplicationContext` 或 `AbstractRefreshableApplicationContext `派生。例如：` AnnotationConfigApplicationContext `由 `GenericApplicationContext `派生， `ClassPathXmlApplicationContext `由 `AbstractRefreshableApplicationContext` 派生。

区别：

`GenericApplictionContext`及其子类持有一个单例的固定的`DefaultListableBeanFactory`实例，在创建`GenericApplicationContext`实例的时候就会创建`DefaultListableBeanFactory`实例。固定的意思就是说，即使调用refresh方法，也**不会重新创建BeanFactory实例**。与之对应的就是`AbstractRefreshableApplicationContext`，它实现了所谓的**热刷新**功能，它内部也持有一个`DefaultListableBeanFactory`实例，每次刷新refresh()时都会**销毁当前的BeanFactory实例并重新创建DefaultListableBeanFactory实例**。

​		而且`AbstractRefreshableApplication`是spring框架加载bean定义文件的传统方式，其在调用obtainFreshBeanFactory方法时，就完成了加载bean定义文件的过程；

​		而`GenericApplicationContext`是springboot中常用的上下文对象，它在调用`obtainFreshBeanFactory`方法时，只是设置了bean工厂的序列化id，而bean定义文件的加载过程是在后续`postProcessBeanFactory`中通过调用`ConfigurationClassPostProcessor`  **在bean定义文件加载完成后，`BeanFactoryPostProcessor#postProcessBeanFactory`之前被调用**，通过配置类扫描手动装载beanDefinition到BeanFactory

### 3）准备bean工厂prepareBeanFactory

```java
protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
   // Tell the internal bean factory to use the context's class loader etc.
   //通知内部bean工厂使用上下文的类加载器
   beanFactory.setBeanClassLoader(getClassLoader());
   //为bean定义值中的表达式指定解析策略，即解析el表达式，默认"#{"开头，"}"结尾
   beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
   //在这里new了一个资源编辑注册器ResourceEditorRegistrar，该类实现了PropertyEditorRegistrar接口
   //作用是使用以下资源编辑器去填充给的的注册表：ResourceEditor、InputStreamEditor、InputSourceEditor、FileEditor、Urleditor、UriEditor、ClassEditor、ClassArrayEditor。
   //如果给的注册表是PropertyEditorRegistrySupport类型，编辑器交由该类管理,即管理一系列的PropertyEditorRegistry类型的组件
   beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

   // Configure the bean factory with context callbacks.
   // 向BeanPostProcessor的List中添加一个ApplicationContextAwareProcessor，参数是上下文
   //该类作用是将上下文传递给实现environmentaware、embeddedValueResolveraware、resourceLoaderware、applicationEventPublisheraware、messageSourceAware或applicationContextaware接口的bean。
   beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
   //自动装配忽略给定的类型，添加到忽略的集合中
   beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
   beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
   beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
   beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
   beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
   beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

   // BeanFactory interface not registered as resolvable type in a plain factory.
   // MessageSource registered (and found for autowiring) as a bean.
   beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
   beanFactory.registerResolvableDependency(ResourceLoader.class, this);
   beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
   beanFactory.registerResolvableDependency(ApplicationContext.class, this);

   // Register early post-processor for detecting inner beans as ApplicationListeners.
   // 该BeanPostProcessor检测那些实现了ApplicationListener接口的bean（通过@Component注解声明的Listener，而非通过spring.factories加载的listeners），在它们创建时初始化之后，将它们添加到应用上下文的事件多播器上
   //并在这些ApplicationListener bean销毁之前，将它们从应用上下文的事件多播器上移除。
   beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

   // Detect a LoadTimeWeaver and prepare for weaving, if found.
   if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
      beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
      // Set a temporary ClassLoader for type matching.
      beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
   }

   // Register default environment beans.
   //注册默认environment bean，如果beanfactory不存在environment
   if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
      beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
   }
   if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
      beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
   }
   if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
      beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
   }
}
```

综上所述，主要给 bean factory 设定以下默认的配置

- class loader
- BeanExpressionResolver：用来解析 spring 表达式
- PropertyEditorRegistrar: 属性相关
- 添加 ApplicationContextAwareProcessor， 这个 BeanPostProcessor在 bean 实例化后给 bean 注入各类 Aware 属性
- 添加 ApplicationListenerDetector， 这个 BeanPostProcessor找出 ApplicationListener 类型的 bean 并注册到 application context
- 如果有 loadTimeWeaver 的话就添加 BeanPostProcessor: LoadTimeWeaverAwareProcessor 和 ClassLoader :ContextTypeMatchClassLoader
- 注册默认的 bean，包括
  - environment： application context 对应的 environment
  - systemProperties： 一个 Map
  - systemEnvironment： 一个 Map

#### 3.1 扩展点ApplicationContextAwareProcessor

该类主要是在Bean实例化后，判断bean的类型，根据其实现类型来设置各类Aware属性

```java
public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    if (!(bean instanceof EnvironmentAware || bean instanceof EmbeddedValueResolverAware ||
          bean instanceof ResourceLoaderAware || bean instanceof ApplicationEventPublisherAware ||
          bean instanceof MessageSourceAware || bean instanceof ApplicationContextAware)){
        return bean;
    }

    AccessControlContext acc = null;

    if (System.getSecurityManager() != null) {
        acc = this.applicationContext.getBeanFactory().getAccessControlContext();
    }

    if (acc != null) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            invokeAwareInterfaces(bean);
            return null;
        }, acc);
    }
    else {
        invokeAwareInterfaces(bean);
    }

    return bean;
}

private void invokeAwareInterfaces(Object bean) {
    if (bean instanceof EnvironmentAware) {
        ((EnvironmentAware) bean).setEnvironment(this.applicationContext.getEnvironment());
    }
    if (bean instanceof EmbeddedValueResolverAware) {
        ((EmbeddedValueResolverAware) bean).setEmbeddedValueResolver(this.embeddedValueResolver);
    }
    if (bean instanceof ResourceLoaderAware) {
        ((ResourceLoaderAware) bean).setResourceLoader(this.applicationContext);
    }
    if (bean instanceof ApplicationEventPublisherAware) {
        ((ApplicationEventPublisherAware) bean).setApplicationEventPublisher(this.applicationContext);
    }
    if (bean instanceof MessageSourceAware) {
        ((MessageSourceAware) bean).setMessageSource(this.applicationContext);
    }
    if (bean instanceof ApplicationContextAware) {
        ((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
    }
}
```

该类本身并没有扩展点，但是该类内部的`invokeAwareInterfaces`却有6个扩展点可供实现，可以看到，该类用于执行各种驱动接口，在bean实例化之后，属性填充之后，通过执行以上set***的扩展接口，来获取对应容器的变量。因此，可以通过实现上述Aware接口，

在set***方法中获取到aware属性，并做出一些自定义操作。

### 4)  postProcessBeanFactory供子类实现的后置处理

在配置完我们的BeanFactory的各个属性后，便会执行postProcessBeanFactory

**它主要是允许一些AbstractApplicationContext的子类在上下文标准初始化后修改组合的Bean工厂，为我们的bean工厂注册一些特殊的BeanPostProcessors**

```java
/**
 * 在标准初始化后修改应用程序上下文的内部bean工厂。
 * 所有bean定义都将被加载，但是没有bean会被实例化。
 * 这允许在某些应用上下文实现中注册特殊的BeanPostProcessors等。
 *
 * Modify the application context's internal bean factory after its standard
 * initialization. All bean definitions will have been loaded, but no beans
 * will have been instantiated yet. This allows for registering special
 * BeanPostProcessors etc in certain ApplicationContext implementations.
 * @param beanFactory the bean factory used by the application context 应用环境下的Bean工厂
 */
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
}
```

例如：

在AbstractRefreshableWebApplicationContext类中，其主要是装载了一个ServletContextAwareProcessor，方便上下文持有ServletContext，并设置了应用的作用范围以及环境相关的bean

```java
/**
 * Register request/session scopes, a {@link ServletContextAwareProcessor}, etc.
 * 注册request/session scopes，一个ServletContextAwareProcessor处理器等。
 */
@Override
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    //ServletContextAwareProcessor中拿到应用上下文持有的servletContext引用和servletConfig引用
    //1.添加ServletContextAwareProcessor处理器
    beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext, this.servletConfig));

    //在自动注入时忽略指定的依赖接口
    //通常被应用上下文用来注册以其他方式解析的依赖项
    beanFactory.ignoreDependencyInterface(ServletContextAware.class);
    beanFactory.ignoreDependencyInterface(ServletConfigAware.class);

    //2.注册web应用的scopes
    WebApplicationContextUtils.registerWebApplicationScopes(beanFactory, this.servletContext);
    //3.注册和环境有关的beans
    WebApplicationContextUtils.registerEnvironmentBeans(beanFactory, this.servletContext, this.servletConfig);
}
```

而在GenericApplicationContext的某个实现类AnnotationConfigServletWebServerApplicationContext类中，其主要是去扫描装载我们启动类，这里以及在上一阶段准备容器环节加载到BeanFactory工厂中了。

```java
@Override
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
   super.postProcessBeanFactory(beanFactory);
   if (!ObjectUtils.isEmpty(this.basePackages)) {
      this.scanner.scan(this.basePackages);
   }
   if (!this.annotatedClasses.isEmpty()) {
      this.reader.register(ClassUtils.toClassArray(this.annotatedClasses));
   }
}
```

### 5）关键步骤--invokeBeanFactoryPostProcessors调用Bean工厂的后置处理器

紧接着就是最为关键的一步，`invokeBeanFactoryPostProcessors`（bean 都还没有初始化，可以操作 beanFacotry 达到修改 bean 的定义，添加 bean 定义的效果）

`invokeBeanFactoryPostProcessors`方法，负责激活各种 BeanFactory 处理器，以及两个核心接口的调用：

- ```
  BeanDefinitionRegistryPostProcessor
  ```

  - 实际完成了对其实现类中`postProcessBeanDefinitionRegistry`方法的调用，**完成对BeanDefinition的新增、修改**；

- ```
  BeanFactoryPostProcessor
  ```

  - 实际完成了对其实现类中`postProcessBeanFactory`方法的调用，**在bean实例化前修改bean的属性**

在调用BeanFactoryPostProcessors前，我们得清楚它的来源，主要有以下两点：

1 . `AbstractApplicationContext` 的 `beanFactoryPostProcessors` 成员，可以通过编程的方式在 `ApplicationContext `refresh 之前通过 `addBeanFactoryPostProcessor` 加入； 

2 .`beanFactory` 中注册的 `BeanFactoryPostProcessor` 类型的 bean

接下来，我们结合源码分析一下这一步具体做了哪些事情

首先，通过AbstractApplicationContext的代理对象，调用BeanFactory的后置处理器，其中`postProcessBeanDefinitionRegistry`优于postProcessBeanFactory

```java
protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
   //核心步骤，分别调用postProcessBeanDefinitionRegistry和postProcessBeanFactory，前者优先调用
   PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

   // Detect a LoadTimeWeaver and prepare for weaving, if found in the meantime
   // (e.g. through an @Bean method registered by ConfigurationClassPostProcessor)
   if (beanFactory.getTempClassLoader() == null && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
      beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
      beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
   }
}
```

在触发后置处理方法时，会获取AbstractApplicationContext装载的beanFactoryPostProcessors对象

![image-20220118114652643](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220118114652643.png)

该集合中的几个处理器装载时机分别在下图几个阶段添加

![image-20220118114834545](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220118114834545.png)

这也是spring留给用户的扩展点之一，可以通过`ApplicationContext.addBeanFactoryPostProcessor`来完成`BeanFactoryPostProcessors`的注入；其支持在refresh前的各个阶段，例如：

#### 5.1 自定义实现BeanFactoryPostProcessor

1.自定义一个监听器，在创建好上下文对象的阶段后，通过实现`ApplicationContextAware`接口，来获取我们的应用上下文，然后在事件的触发函数中，调用`addBeanFactoryPostProcessor`方法，来完成自定义`BeanFactoryPostProcessor`的注入过程

2.在`准备上下文prepareContext`阶段，通过自定义`ApplicationContextInitializer`在容器刷新阶段前，执行回调函数`initialize()`，在函数中注入我们的自定义`BeanFactoryPostProcessor`	

3.通过@Component注解声明自定义BeanFactoryPostProcessor，使其在调用`invokeBeanFactoryPostProcessors`时，

具体的BeanFactoryPostProcessor实现见代码模块

[springboot-beanFactory-postProcessor](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-beanFactory-postProcessor)

接下来，就会调用后置处理器的关键步骤`invokeBeanFactoryPostProcessors`，以下代码，我们分为七个步骤讲解：

1.入参中的`BeanFactoryPostProcessor`，按照是否实现了`BeanDefinitionRegistryPostProcessor`，分别放入两个集合：`registryProcessors`和`regularPostProcessors`；并调用`postProcessBeanDefinitionRegistry`

2.找出所有实现了`BeanDefinitionRegistryPostProcessor`接口和`PriorityOrdered`接口的bean，放入`registryProcessors`集合，放入根据`PriorityOrdered`接口来排序，然后这些bean会被`invokeBeanDefinitionRegistryPostProcessors`方法执行；

3.找出所有实现了`BeanDefinitionRegistryPostProcessor`接口和`Ordered`接口的bean，放入`registryProcessors`集合，放入根据`PriorityOrdered`接口来排序，然后这些bean会被`invokeBeanDefinitionRegistryPostProcessors`方法执行；
4.对于那些实现了`BeanDefinitionRegistryPostProcessor`接口，但是没有实现`PriorityOrdered`和`Ordered`的bean也被找出来，然后这些bean会被`invokeBeanDefinitionRegistryPostProcessors`方法执行；
5.入参中的`BeanFactoryPostProcessor`，没有实现`BeanDefinitionRegistryPostProcessor`的那些bean，被`invokeBeanDefinitionRegistryPostProcessors`;

6.找出实现了`BeanFactoryPostProcessor`接口的bean，**注意这里已将面实现了`BeanDefinitionRegistryPostProcessor`接口的bean给剔除了**，将这些bean分为三类：实现了`PriorityOrdered`接口的放入`priorityOrderedPostProcessors`，实现了`Ordered`接口的放入`orderedPostProcessorNames`，其他的放入`nonOrderedPostProcessorNames`，**这段代码是关键，因为我们自定义的实现BeanFactoryPostProcessor接口的bean就会在此处被找出来。**

7.`priorityOrderedPostProcessors`和`orderedPostProcessorNames`这两个集合，都是先做排序再调用`invokeBeanDefinitionRegistryPostProcessors`方法，最后是`nonOrderedPostProcessorNames`集合，也被传入`invokeBeanDefinitionRegistryPostProcessors`方法；

#### 5.2 触发BeanDefinitionRegistryPostProcessor添加、修改BeanDefinition

首先执行第一步：触发`invokeBeanFactoryPostProcessors`时会先执行以下模块，匹配入参中获取到的`BeanFactoryPostProcessor`，将`BeanDefinitionRegistryPostProcessor`放入`registryProcessors`中，并触发`postProcessBeanDefinitionRegistry`方法；然后将剩余的`BeanFactoryPostProcessor`放到`regularPostProcessors`，共后续触发调用

```java
BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

//入参中的BeanFactoryPostProcessors，按照是否实现了BeanDefinitionRegistryPostProcessor，分别放入两个集合：registryProcessors和regularPostProcessors;并触发`postProcessBeanDefinitionRegistry`方法
for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
    if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
        BeanDefinitionRegistryPostProcessor registryProcessor =
            (BeanDefinitionRegistryPostProcessor) postProcessor;
        //调用BeanDefinitionRegistryPostProcessor，
        registryProcessor.postProcessBeanDefinitionRegistry(registry);
        registryProcessors.add(registryProcessor);
    }
    else {
        regularPostProcessors.add(postProcessor);
    }
}
```

其中，`beanFactoryPostProcessors`信息为下图所示，前两者为registry类型

![image-20220118171956310](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220118171956310.png)

![image-20220118172041402](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220118172041402.png)

然后执行第二步：找出所有实现了`BeanDefinitionRegistryPostProcessor`接口和`PriorityOrdered`接口的bean，放入`registryProcessors`集合，放入根据`PriorityOrdered`接口来排序，然后这些bean会被`invokeBeanDefinitionRegistryPostProcessors`方法执行；

```java
// Do not initialize FactoryBeans here: We need to leave all regular beans
// uninitialized to let the bean factory post-processors apply to them!
// Separate between BeanDefinitionRegistryPostProcessors that implement
// PriorityOrdered, Ordered, and the rest.
List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
String[] postProcessorNames =
    beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
for (String ppName : postProcessorNames) {
    if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
        currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
        processedBeans.add(ppName);
    }
}
//依据PriorityOrderd接口实现进行排序
sortPostProcessors(currentRegistryProcessors, beanFactory);
registryProcessors.addAll(currentRegistryProcessors);
//调用所有实现了BeanDefinitionRegistryPostProcessor的类的postProcessBeanDefinitionRegistry
invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
currentRegistryProcessors.clear();
```

在这一步中，有一个关键的处理器`ConfigurationClassPostProcessor`，通过该处理器来加载`@Configuration`声明的配置类，并装载引用的BeanDefinition至Bean工厂中；

![image-20220118173135905](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220118173135905.png)

![image-20220119104906158](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220119104906158.png)

##### 5.2.1 ConfigurationPostProcessor详解

###### 1.configurationPostProcessor的初始化

那么`ConfigurationPostProcessor`是何时加载到BeanFactory中并实例化的呢？

实际上，在创建`ApplicationContext`上下文对象时，其构造函数中会新建一个`AnnotatedBeanDefinitionReader`

```java
public AnnotationConfigServletWebServerApplicationContext() {
   this.reader = new AnnotatedBeanDefinitionReader(this);
   this.scanner = new ClassPathBeanDefinitionScanner(this);
}

//AnnotatedBeanDefinitionReader构造函数
public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
    this(registry, getOrCreateEnvironment(registry));
}
```

在`AnnotatedBeanDefinitionReader`构造函数中，会去调用`registerAnnotationConfigProcessors`，在调用过程中，会注册`ConfigurationClassPostProcessor`，并以`org.springframework.context.annotation.internalConfigurationAnnotationProcessor`命名

```java
public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry, Environment environment) {
   Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
   Assert.notNull(environment, "Environment must not be null");
   this.registry = registry;
   this.conditionEvaluator = new ConditionEvaluator(registry, environment, null);
   AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
}
```

![image-20220118174305551](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220118174305551.png)

###### 2.postProcessBeanDefinitionRegistry执行流程

紧接着，我们继续跟进ConfigurationClassPostProcessor中的postProcessBeanDefinitionRegistry的实现

```java
@Override
public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
   int registryId = System.identityHashCode(registry);
   if (this.registriesPostProcessed.contains(registryId)) {
      throw new IllegalStateException(
            "postProcessBeanDefinitionRegistry already called on this post-processor against " + registry);
   }
   if (this.factoriesPostProcessed.contains(registryId)) {
      throw new IllegalStateException(
            "postProcessBeanFactory already called on this post-processor against " + registry);
   }
   this.registriesPostProcessed.add(registryId);

   processConfigBeanDefinitions(registry);
}	
```

直接进入其关键步骤`processConfigBeanDefinitions(registry)`，其主要使用了`ConfigurationClassParser`配置类解析器解析`@Configuration`配置类上的诸如`@ComponentScan`、`@Import`、`@Bean`等注解，并尝试发现所有的配置类；还使用了`ConfigurationClassBeanDefinitionReader`注册所发现的所有配置类中的所有Bean定义（需要注意的是此时只加载了启动类的Bean定义，以及部分BeanFactoryPostProcessor的Bean的定义）；结束执行的条件是所有配置类都被发现和处理，相应的bean定义注册到容器

下面分析`processConfigBeanDefinitions(registry)`方法中的具体流程：

![image-20220119105311013](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220119105311013.png)

###### 3.配置类的校验

通过BeanDefinitionRegistry查找当前Spring容器中所有BeanDefinition，并通过`ConfigurationClassUtils.checkConfigurationClassCandidate()` 检查BeanDefinition是否为 **“完全配置类”** 或 **“简化配置类”**，并对配置类做标记，放入集合待后续处理

**完全配置类**和**简化配置类**的区别：如果加了`@Configuration`，那么对应的`BeanDefinition`为`full`，如果加了`@Bean`，`@Component`，`@ComponentScan`，`@Import`，`@ImportResource`这些注解，则为`lite`。`lite`和`full`均表示这个`BeanDefinition`对应的类是一个配置类，但是full标识的配置类中创建的Bean是单例的，每次都会从工厂中获取同一个Bean，而lite无法保证单例

```java
List<BeanDefinitionHolder> configCandidates = new ArrayList<>();
//获取Bean工厂中注册的所有BeanDefinition
String[] candidateNames = registry.getBeanDefinitionNames();

for (String beanName : candidateNames) {
   BeanDefinition beanDef = registry.getBeanDefinition(beanName);
   if (beanDef.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE) != null) {
      if (logger.isDebugEnabled()) {
         logger.debug("Bean definition has already been processed as a configuration class: " + beanDef);
      }
   }
   //获取beanDefinition中的metadata，基于metadata解析beanDefinition为完全配置类还是简化配置类，分别标记存入集合中
   else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef, this.metadataReaderFactory)) {
      configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
   }
}

// Return immediately if no @Configuration classes were found
if (configCandidates.isEmpty()) {
   return;
}
```

```java
public static boolean checkConfigurationClassCandidate(BeanDefinition beanDef, MetadataReaderFactory metadataReaderFactory) {
    ...
    // 根据Bean定义信息，拿到器对应的注解元数据
    AnnotationMetadata metadata = xxx;
    ...

    // 根据注解元数据判断该Bean定义是否是配置类。若是：那是Full模式还是Lite模式
    Map<String, Object> config = metadata.getAnnotationAttributes(Configuration.class.getName());
    if (config != null && !Boolean.FALSE.equals(config.get("proxyBeanMethods"))) {
        beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_FULL);
    } else if (config != null || isConfigurationCandidate(metadata)) {
        beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_LITE);
    } else {
        return false;
    }

    ...

    // 到这。它肯定是一个完整配置（Full or Lite） 这里进一步把@Order排序值放上去
    Integer order = getOrder(metadata);
    if (order != null) {
        beanDef.setAttribute(ORDER_ATTRIBUTE, order);
    }

    return true;
}
```

在获取到配置类的集合后，会对其进行排序、判空处理，并配置相应的Bean命名策略，以及环境信息

```java
// Return immediately if no @Configuration classes were found
if (configCandidates.isEmpty()) {
   return;
}

// Sort by previously determined @Order value, if applicable
configCandidates.sort((bd1, bd2) -> {
   int i1 = ConfigurationClassUtils.getOrder(bd1.getBeanDefinition());
   int i2 = ConfigurationClassUtils.getOrder(bd2.getBeanDefinition());
   return Integer.compare(i1, i2);
});

// Detect any custom bean name generation strategy supplied through the enclosing application context
SingletonBeanRegistry sbr = null;
if (registry instanceof SingletonBeanRegistry) {
   sbr = (SingletonBeanRegistry) registry;
   if (!this.localBeanNameGeneratorSet) {
      BeanNameGenerator generator = (BeanNameGenerator) sbr.getSingleton(
            AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR);
      if (generator != null) {
         this.componentScanBeanNameGenerator = generator;
         this.importBeanNameGenerator = generator;
      }
   }
}

if (this.environment == null) {
   this.environment = new StandardEnvironment();
}
```

###### 4.parse()解析

紧接着就是核心步骤，通过 `ConfigurationClassParser解析器` parse解析配置类集合，尝试通过它们找到其它配置类

**重点！！！**

```
parse()`方法会解析配置类上的注解(ComponentScan扫描出的类，@Import注册的类，以及@Bean方法定义的类)，解析完以后(解析成ConfigurationClass类)，会将解析出的结果放入到`parser`的`configurationClasses`这个属性中(这个属性是个Map)。
parse会将@Import注解要注册的类解析为BeanDefinition，但是不会把解析出来的BeanDefinition放入到BeanDefinitionMap中，真正放入到map中是在这一行代码实现的: `this.reader.loadBeanDefinitions(configClasses)
```

**后续通过ImportSelector导入的类同样会通过ConfigurationClassParser进行解析，通过doProcessConfigurationClass方法解析成ConfigurationClass类，然后统一放到configurationClasses中**，然后在后续parse解析完成后，再从configurationClasses中去除放到BeanDefinitionMap中

```java
// Parse each @Configuration class
ConfigurationClassParser parser = new ConfigurationClassParser(
      this.metadataReaderFactory, this.problemReporter, this.environment,
      this.resourceLoader, this.componentScanBeanNameGenerator, registry);

Set<BeanDefinitionHolder> candidates = new LinkedHashSet<>(configCandidates);
Set<ConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());
do {
   // candidates即为启动类对应的BeanDefinition，此时只加载了启动类的BeanDefinition和部分BeanFactoryPostProcessor的BeanDefinition，且只有启动类声明了Configuration注解
   parser.parse(candidates);
   parser.validate();

   ...
   this.reader.loadBeanDefinitions(configClasses);
   ...
}
while (!candidates.isEmpty());
```

深入查看parse具体解析过程，可以看到其核心处理都是调用`processConfigurationClass()`

```java
public void parse(Set<BeanDefinitionHolder> configCandidates) {
    this.deferredImportSelectors = new LinkedList<>();
    // 根据BeanDefinition类型的不同，调用parse()不同的重载方法
    // 实际上最终都是调用processConfigurationClass()方法
    for (BeanDefinitionHolder holder : configCandidates) {
        BeanDefinition bd = holder.getBeanDefinition();
        try {
            if (bd instanceof AnnotatedBeanDefinition) {
                parse(((AnnotatedBeanDefinition) bd).getMetadata(), holder.getBeanName());
            }else if (bd instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) bd).hasBeanClass()) {
                parse(((AbstractBeanDefinition) bd).getBeanClass(), holder.getBeanName());
            }else {
                parse(bd.getBeanClassName(), holder.getBeanName());
            }
        }
    }
    // 处理延迟importSelector
    processDeferredImportSelectors();
}
```

![image-20220119110322297](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220119110322297.png)

在`processConfigurationClass`中，它会通过`doProcessConfigurationClass`去循环解析配置类

```java
protected void processConfigurationClass(ConfigurationClass configClass) throws IOException {
    // 处理配置类，由于配置类可能存在父类(若父类的全类名是以java开头的，则除外)，所以需要将configClass变成sourceClass去解析，然后返回sourceClass的父类。
    // 如果此时父类为空，则不会进行while循环去解析，如果父类不为空，则会循环的去解析父类
    // SourceClass的意义：简单的包装类，目的是为了以统一的方式去处理带有注解的类，不管这些类是如何加载的
    // 如果无法理解，可以把它当做一个黑盒，不会影响看spring源码的主流程
    SourceClass sourceClass = asSourceClass(configClass);
    do {
    // 核心处理逻辑
        sourceClass = doProcessConfigurationClass(configClass, sourceClass);
    }
    while (sourceClass != null);
    // 将解析的配置类存储起来，这样回到parse()方法时，能取到值
    this.configurationClasses.put(configClass, configClass);
}
```

`doProcessConfigurationClass()`方法中，执行流程如下:

- (1) 处理内部类，如果内部类也是一个配置类(判断一个类是否是一个配置类，通过`ConfigurationClassUtils.checkConfigurationClassCandidate()`可以判断)。
- (2) 处理属性资源文件，加了`@PropertySource`注解。
- (3) 首先解析出类上的`@ComponentScan`和`@ComponentScans`注解，然后根据配置的扫描包路径，利用ASM技术(ASM技术是一种操作字节码的技术)扫描出所有需要交给Spring管理的类，获取这些@Component声明的组件类的bean definition，并将其装载到Bean工厂中的bean definition map中；由于扫描出的类中可能也被加了`@ComponentScan`和`@ComponentScans`注解，因此需要进行递归解析，直到所有加了这两个注解的类被解析完成。
- (4) 处理`@Import`注解。通过`@Import`注解，有三种方式可以将一个Bean注册到Spring容器中，分别是`ImportSelector`、`DeferredImportSelector`和`ImportSelectorRegistrar`
- (5) 处理`@ImportResource`注解，解析配置文件。
- (6) 处理加了`@Bean`注解的方法。
- (7) 通过`processInterfaces()`处理接口的默认方法，从JDK8开始，接口中的方法可以有自己的默认实现，因此，如果这个接口中的方法也加了@Bean注解，也需要被解析。(很少用)
- (8) 解析父类，如果被解析的配置类继承了某个类，那么配置类的父类也会被进行解析`doProcessConfigurationClass()`(父类是JDK内置的类例外，即全类名以java开头的)。

关于第(7)步，举个例子解释下。如下代码示例，`AppConfig`类加了`Configuration`注解，是一个配置类，且实现了`AppConfigInterface`接口，这个接口中有一个默认的实现方法(JDK8开始，接口中的方法可以有默认实现)，该方法上添加了`@Bean`注解。这个时候，经过第(7)步的解析，会向spring容器中添加一个`InterfaceMethodBean`类型的bean。

```java
@Configuration
public class AppConfig implements AppConfigInterface{
}

public interface AppConfigInterface {
    @Bean
    default InterfaceMethodBean interfaceMethodBean() {
        return new InterfaceMethodBean();
    }
}
```

源码如下：

```java
protected final SourceClass doProcessConfigurationClass(
			ConfigurationClass configClass, SourceClass sourceClass, Predicate<String> filter)
			throws IOException {

		if (configClass.getMetadata().isAnnotated(Component.class.getName())) {
			// Recursively process any member (nested) classes first
            // 1、首先处理内部类，处理内部类时，最终还是调用doProcessConfigurationClass()方法
			processMemberClasses(configClass, sourceClass, filter);
		}

		// Process any @PropertySource annotations
        // 2、处理属性资源文件，解析扫描的类中使用了@PropertySource注解的Bean，调用processPropertyeSource方法，将注解中指定的配置文件中的配置添加到environment环境变量中
		for (AnnotationAttributes propertySource : AnnotationConfigUtils.attributesForRepeatable(
				sourceClass.getMetadata(), PropertySources.class,
				org.springframework.context.annotation.PropertySource.class)) {
			if (this.environment instanceof ConfigurableEnvironment) {
				processPropertySource(propertySource);
			}
			else {
				logger.info("Ignoring @PropertySource annotation on [" + sourceClass.getMetadata().getClassName() +
						"]. Reason: Environment must implement ConfigurableEnvironment");
			}
		}

		// Process any @ComponentScan annotations
    	// 3、处理@ComponentScan或者@ComponentScans注解
    	// 3.1 先找出类上的@ComponentScan和@ComponentScans注解的所有属性(例如basePackages等属性值)
		Set<AnnotationAttributes> componentScans = AnnotationConfigUtils.attributesForRepeatable(
				sourceClass.getMetadata(), ComponentScans.class, ComponentScan.class);
		if (!componentScans.isEmpty() &&
				!this.conditionEvaluator.shouldSkip(sourceClass.getMetadata(), ConfigurationPhase.REGISTER_BEAN)) {
            
			for (AnnotationAttributes componentScan : componentScans) {
				// The config class is annotated with @ComponentScan -> perform the scan immediately
                // 3.2 解析@ComponentScan和@ComponentScans配置的扫描的包所包含的类
            	// 比如 basePackages = cn.com.study, 那么在这一步会扫描出这个包及子包下的class，然后将其解析成BeanDefinition，装载到BeanDefinitionMap中，这也是为什么创建的组件类需要在启动类的根路径下，否则就加载不到
            	// (BeanDefinition可以理解为等价于BeanDefinitionHolder)
				Set<BeanDefinitionHolder> scannedBeanDefinitions =
						this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());
				// Check the set of scanned definitions for any further config classes and parse recursively if needed
                // 3.3 通过上一步扫描包cn.com.study下的类，有可能扫描出来的bean中可能也添加了ComponentScan或者ComponentScans注解.
            	//所以这里需要循环遍历一次，进行递归(parse)，继续解析，直到解析出的类上没有ComponentScan和ComponentScans
            	// (这时3.1这一步解析出componentScans为空列表，不会进入到if语句，递归终止)
				for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
					BeanDefinition bdCand = holder.getBeanDefinition().getOriginatingBeanDefinition();
					if (bdCand == null) {
						bdCand = holder.getBeanDefinition();
					}
                    // 同样，这里会调用ConfigurationClassUtils.checkConfigurationClassCandidate()方法来判断类是否是一个配置类
					if (ConfigurationClassUtils.checkConfigurationClassCandidate(bdCand, this.metadataReaderFactory)) {
						parse(bdCand.getBeanClassName(), holder.getBeanName());
					}
				}
			}
		}

		// Process any @Import annotations
    	// 4.处理Import注解注册的bean，这一步只会将import注册的bean变为ConfigurationClass,不会变成BeanDefinition
    	// 而是在parse()执行完后，通过this.reader.loadBeanDefinitions()变成BeanDefinition，再放入到BeanDefinitionMap中
		processImports(configClass, sourceClass, getImports(sourceClass), filter, true);

		// Process any @ImportResource annotations
    	// 5.处理@ImportResource注解引入的配置文件
		AnnotationAttributes importResource =
				AnnotationConfigUtils.attributesFor(sourceClass.getMetadata(), ImportResource.class);
		if (importResource != null) {
			String[] resources = importResource.getStringArray("locations");
			Class<? extends BeanDefinitionReader> readerClass = importResource.getClass("reader");
			for (String resource : resources) {
				String resolvedResource = this.environment.resolveRequiredPlaceholders(resource);
				configClass.addImportedResource(resolvedResource, readerClass);
			}
		}

		// Process individual @Bean methods
    	// 6.处理加了@Bean注解的方法
		Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(sourceClass);
		for (MethodMetadata methodMetadata : beanMethods) {
			configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
		}

		// Process default methods on interfaces
    	// 7.处理接口的默认方法，可能在接口的默认实现上声明了@Bean注解
		processInterfaces(configClass, sourceClass);

		// Process superclass, if any
    	// 8.解析父类，若父类时配置类，会重新执行上述过程
		if (sourceClass.getMetadata().hasSuperClass()) {
			String superclass = sourceClass.getMetadata().getSuperClassName();
			if (superclass != null && !superclass.startsWith("java") &&
					!this.knownSuperclasses.containsKey(superclass)) {
				this.knownSuperclasses.put(superclass, configClass);
				// Superclass found, return its annotation metadata and recurse
				return sourceClass.getSuperClass();
			}
		}

		// No superclass -> processing is complete
		return null;
	}
```

###### 5.processImports--@Import注解解析及使用

`processImports`方法中包含了对`ImportSelector`实现类和`ImportBeanDefinitionRegistrar`实现类的处理，以及未实现这些接口的类的处理；下面，我们根据代码看一下其具体实现步骤

```java
private void processImports(ConfigurationClass configClass, SourceClass currentSourceClass,
			Collection<SourceClass> importCandidates, Predicate<String> exclusionFilter,
			boolean checkForCircularImports) {
    
    ...
        
    for (SourceClass candidate : importCandidates) {
        if (candidate.isAssignable(ImportSelector.class)) {
            // Candidate class is an ImportSelector -> delegate to it to determine imports
            // 实例化ImportSelector类
            Class<?> candidateClass = candidate.loadClass();
            ImportSelector selector = ParserStrategyUtils.instantiateClass(candidateClass, ImportSelector.class,this.environment, this.resourceLoader, this.registry);
            Predicate<String> selectorFilter = selector.getExclusionFilter();
            if (selectorFilter != null) {
                exclusionFilter = exclusionFilter.or(selectorFilter);
            }
            // DeferredImportSelector需要延迟处理，先缓存至deferredImportSelectorHandler
            if (selector instanceof DeferredImportSelector) {
                this.deferredImportSelectorHandler.handle(configClass, (DeferredImportSelector) selector);
            }
            else {
                // 调用selectImports方法，获取需要动态加载的Bean
                String[] importClassNames = selector.selectImports(currentSourceClass.getMetadata());
                Collection<SourceClass> importSourceClasses = asSourceClasses(importClassNames, exclusionFilter);
                // 若加载的类中还存在Import注解，则迭代调用processImports处理Import声明的配置类
                processImports(configClass, currentSourceClass, importSourceClasses, exclusionFilter, false);
            }
        }
        else if (candidate.isAssignable(ImportBeanDefinitionRegistrar.class)) {
            // Candidate class is an ImportBeanDefinitionRegistrar ->
            // delegate to it to register additional bean definitions
            // ImportBeanDefinitionRegistrar实现类缓存至configClass中，最后交由ConfigurationClassPostProcessor的processConfigBeanDefinitions方法中this.reader.loadBeanDefinitions(configClasses)处理
            Class<?> candidateClass = candidate.loadClass();
            ImportBeanDefinitionRegistrar registrar =
                ParserStrategyUtils.instantiateClass(candidateClass, ImportBeanDefinitionRegistrar.class,
                                                     this.environment, this.resourceLoader, this.registry);
            configClass.addImportBeanDefinitionRegistrar(registrar, currentSourceClass.getMetadata());
        }
        else {
            // Candidate class not an ImportSelector or ImportBeanDefinitionRegistrar ->
            // process it as an @Configuration class
            // 非ImportSelector和ImportBeanDefinitionRegistrar类(对应上面通过SelectImporter.selectImports方法获取到的需要动态加载的Bean)交由ConfigurationClassPostProcessor的doProcessConfigurationClass进行处理，见上述第4阶段parse()解析
            this.importStack.registerImport(
                currentSourceClass.getMetadata(), candidate.getMetadata().getClassName());
            // 通过processConfigurationClass将导入的class交由ConfigurationClassPostProcessor的doProcessConfigurationClass进行解析parse,解析过程中转化成configurationClass，比暂存到configurationClasses中，在第六步loadBeanDefinition中，在存到BeanDefinitionMap中
            processConfigurationClass(candidate.asConfigClass(configClass), exclusionFilter);
        }
    }
    
    ...
        
}
```

首先，遍历扫描到`@Import`注解的类，并判断其是否是`ImportSelector`实现，若为`DeferredImportSelector`实现类，则将其缓存起来，在parse的最后阶段，通过`this.deferredImportSelectorHandler.process()`来处理；否则，调用selectImports方法来获取需要动态加载的Bean的类，然后通过`processImports`方法来处理加载到的Bean类，若该类`ImportSelector`实现类则继续执行以下逻辑，否则按照`@Configuration`作为配置类交由`ConfigurationClassPostProcessor`的`doProcessConfigurationClass`处理，即重复parse解析阶段

![image-20220121112118018](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220121112118018.png)

若为`ImportBeanDefinitionRegistrar`实现类，则会将其注册到configClasses中，在parse()阶段执行完成后，调用`this.reader.loadBeanDefinition(configClasses)`来执行这些`ImportBeanDefinitionRegistrar`的`registerBeanDefinitions()`注册BeanDefinition至Bean工厂

![image-20220121113055386](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220121113055386.png)

若二者皆不是的话，则会该@Import注解的声明类作为配置类(包含ImportSelector中返回的Bean的类)，交由ConfigurationClassPostProcessor的doProcessConfigurationClass处理

![image-20220121113306580](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220121113306580.png)

在解析完成后，就会调用parse()的最后一步`this.deferredImportSelectorHandler.process();`去调用延迟执行的ImportSelector去导入相应的配置类

###### 5.1 DeferredImportSelector定义

```java
// DeferredImportSelector具体实现都在其内部定义的接口Group中，其自身只包含一个方法定义getImportGroup，返回接口实现中的内部Group接口的实现类，方便应用获取到Group实现进行导入处理
public interface DeferredImportSelector extends ImportSelector {
	// 
	@Nullable
	default Class<? extends Group> getImportGroup() {
		return null;
	}
	
    // 真正执行导入逻辑的实现类
	interface Group {

		// 获取配置类上声明的注解信息，以及导入的selector类型，实现逻辑可自行定义；
        // 例如：启动类上的@EnableAutoConfiguration，其引用了AutoConfigurationImportSelector，在这个selector的group实现中通过process函数去使用appClassLoader加载了所有jar，并获取了所有jar中的spring.factories的EnableAutoConfiguration的实现类，将其封装到下面的Entry的集合中，然后调用selectImports时，就会获取这个Entry集合导入所有自动装配的实现类
		void process(AnnotationMetadata metadata, DeferredImportSelector selector);

		// 导入通过process实现中加载的所有配置类
		Iterable<Entry> selectImports();

		// 封装需要导入的配置类的信息
		class Entry {

			private final AnnotationMetadata metadata;

			private final String importClassName;

			public Entry(AnnotationMetadata metadata, String importClassName) {
				this.metadata = metadata;
				this.importClassName = importClassName;
			}

			/**
			 * Return the {@link AnnotationMetadata} of the importing
			 * {@link Configuration} class.
			 */
			public AnnotationMetadata getMetadata() {
				return this.metadata;
			}

			/**
			 * Return the fully qualified name of the class to import.
			 */
			public String getImportClassName() {
				return this.importClassName;
			}

			...
		}
	}

}
```

这里我们结合EnableAutoConfiguration自动装配的实现来分析DeferredImportSelector的原理

###### 5.2 自动装配原理(DeferredImportSelector实现)

在上述分析过程中，我们可以看到Springboot通过@Import注解加载对应的Selector来导入一系列的BeanDefinition，交由ConfigurationClassPostProcessor解析处理成ConfigurationClass，在后续转换成BeanDefinition装载到IOC容器中。

基于这种模式，就延申出来了一种拓展，可以通过一些自定义的注解，配合@Import注解来实现某些Bean对象装载到IOC容器中的开关的功能，通过是否引用该自定义注解来达到是否注入某些Bean的效果。在springboot中也存在许多这种实现，例如：@EnableAsync等。

但是，用户在引入对应依赖后，可能会出现忘记配置对应注解的现象。为了达到开箱即用的效果（引入依赖立即生效，导入相关功能的Bean对象），Springboot又提供了一种自动装配的方式，即通过`@EnableAutoConfiguration`注解借由`AutoConfigurationImportSelector`导入引用的所有jar包的spring.factories中的EnableAutoConfiguration定义的配置类，通过配置类上面定义的一系列注解（例如，@Bean,@Import,@ComponentScan）来实现相应功能Bean的装载。

**源码分析**

在启动类上声明的注解`@SpringBootApplication`中包含了三个关键的注解:`SpringBootConfiguration`,`EnableAutoConfiguration`，`ComponentScan`

SpringBootConfiguration：用于声明当前类为配置类(实际上该注解又@Configuration构成)

ComponentScan：定义了类加载的一系列策略(在refresh阶段通过ConfigurationClassPostProcessor解析配置类时指定扫描的配置的路径)

EnableAutoConfiguration：提供了自动装配的支持，其由`@AutoConfigurationPackage`和`@Import`注解构成，通过Import注解加载`AutoConfigurationImportSelector`导入加载的所有jar包的spring.factories中的EnableAutoConfiguration配置类

```java
//org.springframework.boot.autoconfigure.SpringBootApplication
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {
	...
}

//org.springframework.boot.autoconfigure.EnableAutoConfiguration
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {

	String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";

	/**
	 * Exclude specific auto-configuration classes such that they will never be applied.
	 * @return the classes to exclude
	 */
	Class<?>[] exclude() default {};

	/**
	 * Exclude specific auto-configuration class names such that they will never be
	 * applied.
	 * @return the class names to exclude
	 * @since 1.3.0
	 */
	String[] excludeName() default {};

}
```

`AutoConfigurationImportSelector`实现了`DeferredImportSelector`，在parse解析配置类的最后一步调用`this.deferredImportSelectorHandler.process()`，在这个处理过程中，它会获取`AutoConfigurationImportSelector`的group接口的实现`AutoConfigurationGroup`导入配置类

首先我们分析一下``this.deferredImportSelectorHandler.process()`的实现，其会触发所有装载的DeferredImportSelector的groups实现

```java
// org.springframework.context.annotation.ConfigurationClassParser$DeferredImportSelectorHandler
public void process() {
    List<DeferredImportSelectorHolder> deferredImports = this.deferredImportSelectors;
    this.deferredImportSelectors = null;
    try {
        if (deferredImports != null) {
            DeferredImportSelectorGroupingHandler handler = new DeferredImportSelectorGroupingHandler();
            deferredImports.sort(DEFERRED_IMPORT_COMPARATOR);
            deferredImports.forEach(handler::register);
            //按照group导入配置类
            handler.processGroupImports();
        }
    }
    finally {
        this.deferredImportSelectors = new ArrayList<>();
    }
}
// org.springframework.context.annotation.ConfigurationClassParser$DeferredImportSelectorGroupingHandler
public void processGroupImports() {
    for (DeferredImportSelectorGrouping grouping : this.groupings.values()) {
        Predicate<String> exclusionFilter = grouping.getCandidateFilter();
        // 调用grouping.getImports()扫描获取需要导入的配置类
        grouping.getImports().forEach(entry -> {
            ...
        });
    }
}

public Iterable<Group.Entry> getImports() {
    for (DeferredImportSelectorHolder deferredImport : this.deferredImports) {
        // 获取启动类上声明的注解信息，并通过@EnableAutoConfiguration注解解析获取spring.factories中的自动配置的实现
        this.group.process(deferredImport.getConfigurationClass().getMetadata(),
                           deferredImport.getImportSelector());
    }
    // 导入上述过程中获取到的enableAutoConfiguration的实现类
    return this.group.selectImports();
}
```

这里我们可以看到，其触发了group接口实现中的process方法来解析获取自动配置的实现信息等

```java
public void process(AnnotationMetadata annotationMetadata, DeferredImportSelector deferredImportSelector) {
    // 判断当前启动类上声明引用的Selector是否是AutoConfigurationImportSelector
    Assert.state(deferredImportSelector instanceof AutoConfigurationImportSelector,
                 () -> String.format("Only %s implementations are supported, got %s",
                                     AutoConfigurationImportSelector.class.getSimpleName(),
                                     deferredImportSelector.getClass().getName()));
    // getAutoConfigurationMetadata获取的是META-INF/spring-autoconfigure-metadata.properties内所有自动配置的条件信息
    AutoConfigurationEntry autoConfigurationEntry = ((AutoConfigurationImportSelector) deferredImportSelector)
        .getAutoConfigurationEntry(getAutoConfigurationMetadata(), annotationMetadata);
    this.autoConfigurationEntries.add(autoConfigurationEntry);
    for (String importClassName : autoConfigurationEntry.getConfigurations()) {
        this.entries.putIfAbsent(importClassName, annotationMetadata);
    }
}

private AutoConfigurationMetadata getAutoConfigurationMetadata() {
    if (this.autoConfigurationMetadata == null) {
        // loadMetadata加载META-INF/spring-autoconfigure-metadata.properties中的所有自动配置的条件信息，该properties文件若未自定义的话，只存在于spring-boot-autoconfiguration中
        this.autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader);
    }
    return this.autoConfigurationMetadata;
}
```

其中，AutoConfigurationMetadataLoader.loadMetadata会加载META-INF/spring-autoconfigure-metadata.properties中的配置条件，其配置了待自动装配的配置类的装配条件

```java
protected static final String PATH = "META-INF/spring-autoconfigure-metadata.properties";

static AutoConfigurationMetadata loadMetadata(ClassLoader classLoader) {
	return loadMetadata(classLoader, PATH);
}
```

![image-20220719141127277](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220719141127277.png)

然后调用getAutoConfigurationEntry来过滤自动装配的Bean

```java
protected AutoConfigurationEntry getAutoConfigurationEntry(AutoConfigurationMetadata autoConfigurationMetadata,AnnotationMetadata annotationMetadata) {
    // 判断是否开启自动装配，即配置spring.boot.enableautoconfiguration为true,默认为true
    if (!isEnabled(annotationMetadata)) {
        return EMPTY_ENTRY;
    }
    // 获取启动类上声明的注解，即@SpringBootApplication
    AnnotationAttributes attributes = getAttributes(annotationMetadata);
    // 获取所有jar的spring.factories中所有自动配置（EnableAutoConfiguration类型）的配置类
    List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
    // 移除重复的配置类，可能在多个jar中的spring.factories声明了同样的实现
    configurations = removeDuplicates(configurations);
    // 获取启动类上声明注解的exclude配置，包含（spring.autoconfigure.exclude指定的autoConfiguration配置类）
    Set<String> exclusions = getExclusions(annotationMetadata, attributes);
    // 校验需要移除的类是否在configuration这个配置类集合中，若不在则抛出异常
    checkExcludedClasses(configurations, exclusions);
    // 移除EnableAutoConfiguration配置类
    configurations.removeAll(exclusions);
    // 加载spring.factories中AutoConfigurationImportFilter的实现（OnBeanCondition，OnClassCondition和OnWebApplicationCondition），并读取从spring-autoconfigure-metadata.properties中加载的装配条件，获取@ConditionalOnBean,@ConditionalOnMissingBean，@ConditionalOnClass,@ConditionalOnMissingClass和@ConditionalOnWebApplication几个注解的装配条件的类，解析是否已经装载对应的Bean,Class等，来决定是否启用集合中的AutoConfiguration配置类，
    configurations = filter(configurations, autoConfigurationMetadata);
    // 触发自动装配事件，将装载的自动配置类和exclude排除的配置类装载到ConditionEvaluationReport进行缓存，然后由ConditionEvaluationReportLoggingListener监听ContextRefreshedEvent和ApplicationFailedEvent事件发生时，打印缓存的自动配置类和排除的配置类
    fireAutoConfigurationImportEvents(configurations, exclusions);
    return new AutoConfigurationEntry(configurations, exclusions);
}

protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes 	attributes) {
    //getSpringFactoriesLoaderFactoryClass返回EnableAutoConfiguration.class,因此loadFactoryNames即是从当前的AppClassLoader获取的jar的spring.factories中配置的EnableAutoConfiguration的实现类
    List<String> configurations = SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(),getBeanClassLoader());
    Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you " + "are using a custom packaging, make sure that file is correct.");
    return configurations;
}
```

在上述获取AutoConfiguration配置类的过程中，主要就是通过类加载器加载jar中所有的spring.factories中的`EnableAutoConfiguration`实现类，这些类再经由启动类上声明的exclude配置以及`spring.autoconfigure.exclude`过滤掉部分autoConfiguration配置类；然后，再通过类加载器加载`AutoConfigurationImportFilter`，去解析@Conditional*等注解，对加载的AutoConfiguration配置类进行过滤，最后再触发`AutoConfigurationImportEvent`事件。

这里我们主要针对这个filter过滤的实现进行分析

```java
private List<String> filter(List<String> configurations, AutoConfigurationMetadata autoConfigurationMetadata) {
    long startTime = System.nanoTime();
    String[] candidates = StringUtils.toStringArray(configurations);
    boolean[] skip = new boolean[candidates.length];
    boolean skipped = false
    // 通过类加载器加载AutoConfigurationImportFilter实现(OnBeanCondition，OnClassCondition和OnWebApplicationCondition),以上几个实现均继承了抽象类FilteringSpringBootCondition
    for (AutoConfigurationImportFilter filter : getAutoConfigurationImportFilters()) {
        // 触发Aware接口实现，主要是为了在上述几个Filter中注册BeanFactory、BeanClassLoader等
        invokeAwareMethods(filter);
        // 过滤实现步骤，调用上述几个Filter的实现，结合spring-autoconfigure-metadata.properties中加载的装配条件中指定的@ConditionalOn*的注解的实现类，来决定是否要过滤集合中的AutoConfiguration配置类
        // OnBeanCondition通过从spring-autoconfigure-metadata.properties加载的装配条件的@ConditionalOnBean,@ConditionalOnMissingBean的实现类，来判断BeanFactory中是否装载了对应的BeanDefinition，从而决定是否加载当前的配置类
        // OnClassCondition通过通过从spring-autoconfigure-metadata.properties加载的装配条件的@ConditionalOnClass,@ConditionalOnMissingClass的实现类，来判断BeanFactory中是否装载了对应的BeanDefinition，从而决定是否加载当前的配置类
        // OnWebApplictionCondition通过通过从spring-autoconfigure-metadata.properties加载的装配条件的@ConditionalOnWebApplication的实现类，来判断BeanFactory中是否装载了对应的BeanDefinition，从而决定是否加载当前的配置类
        boolean[] match = filter.match(candidates, autoConfigurationMetadata);
        for (int i = 0; i < match.length; i++) {
            // 过滤未匹配到的装配条件的配置类，将其置空，不进行后续装配过程
            if (!match[i]) {
                skip[i] = true;
                candidates[i] = null;
                skipped = true;
            }
        }
    }
    // 若装配条件涵盖了所有配置类上声明的条件注解，则直接返回所有配置类，全部进行自动装配
    if (!skipped) {
        return configurations;
    }
    List<String> result = new ArrayList<>(candidates.length);
    for (int i = 0; i < candidates.length; i++) {
        if (!skip[i]) {
            // 过滤掉所有未匹配到装配条件的配置类，仅保留需要装配的配置类
            result.add(candidates[i]);
        }
    }
    if (logger.isTraceEnabled()) {
        int numberFiltered = configurations.size() - result.size();
        logger.trace("Filtered " + numberFiltered + " auto configuration class in "
                     + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " ms");
    }
    return new ArrayList<>(result);
}
```

获得筛选后的autoConfiguration配置类后，就会调用`fireAutoConfigurationImportEvents`，获取`AutoConfigurationImportEventListener`的实现类`ConditionEvaluationReportAutoConfigurationImportListener`,并触发`AutoConfigurationImportEvent`事件，然后缓存需要装载的AutoConfiguration配置类和exclude排除的配置类到`ConditionEvaluationReport`中

```java
private void fireAutoConfigurationImportEvents(List<String> configurations, Set<String> exclusions) {
   //加载AutoConfigurationImportListener实现类ConditionEvaluationReportAutoConfigurationImportListener
   List<AutoConfigurationImportListener> listeners = getAutoConfigurationImportListeners();
   if (!listeners.isEmpty()) {
      AutoConfigurationImportEvent event = new AutoConfigurationImportEvent(this, configurations, exclusions);
      for (AutoConfigurationImportListener listener : listeners) {
         invokeAwareMethods(listener);
         // 触发AutoConfigurationImportEvent事件，调用ConditionEvaluationReportAutoConfigurationImportListener的onAutoConfigurationImportEvent方法处理事件，将需要装载的AutoConfiguration配置类和通过exclude排除的AutoConfiguration配置类缓存到ConditionEvaluationReport中，待后续
         listener.onAutoConfigurationImportEvent(event);
      }
   }
}

// 通过类加载器扫描spring.factories中的AutoConfigurationImportListener实现类
protected List<AutoConfigurationImportListener> getAutoConfigurationImportListeners() {
    return SpringFactoriesLoader.loadFactories(AutoConfigurationImportListener.class, this.beanClassLoader);
}

org.springframework.boot.autoconfigure.AutoConfigurationImportListener=\
org.springframework.boot.autoconfigure.condition.ConditionEvaluationReportAutoConfigurationImportListener
    
// org.springframework.boot.autoconfigure.condition.ConditionEvaluationReportAutoConfigurationImportListener
public void onAutoConfigurationImportEvent(AutoConfigurationImportEvent event) {
    if (this.beanFactory != null) {
        // 缓存配置类和排除的配置类到ConditionEvaluationReport中
        ConditionEvaluationReport report = ConditionEvaluationReport.get(this.beanFactory);
        report.recordEvaluationCandidates(event.getCandidateConfigurations());
        report.recordExclusions(event.getExclusions());
    }
}
```

后续`ConditionEvaluationReportLoggingListener`内部实现类`ConditionEvaluationReportListener`监听`ContextRefreshedEvent`和`ApplicationFailedEvent`时，打印`ConditionEvaluationReport`缓存的配置类和排除的配置类信息

```java
// ConditionEvaluationReportLoggingListener实现了ApplicationContextInitializer接口类，其创建SpringbootApplication类时，在构造函数中通过setInitializer就完成了实例化，而其内部类ConditionEvaluationReportListener监听器则是在initialize方法中装载到上下文的监听器集合中的，initialize操作在第五步准备容器prepareContext时，遍历执行了所有Initializer实现的initialize方法进行了初始化
public class ConditionEvaluationReportLoggingListener
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	...

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		applicationContext.addApplicationListener(new ConditionEvaluationReportListener());
		if (applicationContext instanceof GenericApplicationContext) {
			// Get the report early in case the context fails to load
			this.report = ConditionEvaluationReport.get(this.applicationContext.getBeanFactory());
		}
	}

	protected void onApplicationEvent(ApplicationEvent event) {
		ConfigurableApplicationContext initializerApplicationContext = this.applicationContext;
		if (event instanceof ContextRefreshedEvent) {
			if (((ApplicationContextEvent) event).getApplicationContext() == initializerApplicationContext) 			{
				logAutoConfigurationReport();
			}
		}
		else if (event instanceof ApplicationFailedEvent
				&& ((ApplicationFailedEvent) event).getApplicationContext()==initializerApplicationContext) 		{
			logAutoConfigurationReport(true);
		}
	}

	private void logAutoConfigurationReport() {
		logAutoConfigurationReport(!this.applicationContext.isActive());
	}

	public void logAutoConfigurationReport(boolean isCrashReport) {
		if (this.report == null) {
			if (this.applicationContext == null) {
				this.logger.info("Unable to provide the conditions report due to missing ApplicationContext");
				return;
			}
			this.report = ConditionEvaluationReport.get(this.applicationContext.getBeanFactory());
		}
		if (!this.report.getConditionAndOutcomesBySource().isEmpty()) {
			if (this.getLogLevelForReport().equals(LogLevel.INFO)) {
				if (this.logger.isInfoEnabled()) {
					this.logger.info(new ConditionEvaluationReportMessage(this.report));
				}
				else if (isCrashReport) {
					logMessage("info");
				}
			}
			else {
				if (this.logger.isDebugEnabled()) {
					this.logger.debug(new ConditionEvaluationReportMessage(this.report));
				}
				else if (isCrashReport) {
					logMessage("debug");
				}
			}
		}
	}

	private void logMessage(String logLevel) {
		this.logger.info(String.format("%n%nError starting ApplicationContext. To display the "
				+ "conditions report re-run your application with '" + logLevel + "' enabled."));
	}

	private class ConditionEvaluationReportListener implements GenericApplicationListener {

		@Override
		public int getOrder() {
			return Ordered.LOWEST_PRECEDENCE;
		}

		@Override
		public boolean supportsEventType(ResolvableType resolvableType) {
			Class<?> type = resolvableType.getRawClass();
			if (type == null) {
				return false;
			}
			return ContextRefreshedEvent.class.isAssignableFrom(type)
					|| ApplicationFailedEvent.class.isAssignableFrom(type);
		}

		@Override
		public boolean supportsSourceType(Class<?> sourceType) {
			return true;
		}

		@Override
		public void onApplicationEvent(ApplicationEvent event) {
            // ContextRefreshedEvent和ApplicationFailedEvent事件触发时，打印自动配置类和排除的配置类
			ConditionEvaluationReportLoggingListener.this.onApplicationEvent(event);
		}

	}

}
```

至此，`AutoConfigurationImportSelector`通过process处理需要进行自动装配的AutoConfiguration配置类的过程就完成了，其将所有需要装载的AutoConfiguration配置类和排除的配置类都加载到了`autoConfigurationEntries`中，然后调用`AutoConfigurationImportSelector`内部group接口实现的selectImport方法来导入对应的配置类

```java
private static class AutoConfigurationGroup
			implements DeferredImportSelector.Group, BeanClassLoaderAware, BeanFactoryAware, ResourceLoaderAware {
    ...     
	public Iterable<Entry> selectImports() {
        if (this.autoConfigurationEntries.isEmpty()) {
            return Collections.emptyList();
        }
        // 获取所有排除的AutoConfiguration配置类
        Set<String> allExclusions = this.autoConfigurationEntries.stream()          .map(AutoConfigurationEntry::getExclusions).flatMap(Collection::stream).collect(Collectors.toSet());
        // 获取所有需要装载的AutoConfigurationPeizhi类
        Set<String> processedConfigurations = this.autoConfigurationEntries.stream()
            .map(AutoConfigurationEntry::getConfigurations).flatMap(Collection::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        processedConfigurations.removeAll(allExclusions);

        // 返回所有需要装载的AutoConfiguration配置类
        return sortAutoConfigurations(processedConfigurations, getAutoConfigurationMetadata()).stream()
            .map((importClassName) -> new Entry(this.entries.get(importClassName), importClassName))
            .collect(Collectors.toList());
    }
}
```

在通过DeferredImportSelector类型的AutoConfigurationImportSelector完成自动配置类的装配后，会在ConfigurationPostProcessor执行解析的最后一步通过`this.reader.loadBeanDefinition()`将parse()阶段解析生成的配置类转化为BeanDefinition并装载的BeanFactory中。等待后续需要对Bean进行实例化时，就会加载BeanFactory中的定义文件，进行Bean的实例化（例如，自动注入某个Bean时，就会实例化Bean）

至此，自动装配的整个构成就完成了。

###### 5.3 @Import注解使用

在讲解`@Import`注解使用前，首先谈一下`@Enable***`，以`@EnableAsync`为例：

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AsyncConfigurationSelector.class)
public @interface EnableAsync {
    Class<? extends Annotation> annotation() default Annotation.class;

    AdviceMode mode() default AdviceMode.PROXY;java

    int order() default Ordered.LOWEST_PRECEDENCE;
}
```

从以上代码可见，使异步调用生效的关键是`@Import(AsyncConfigurationSelector.class)`，通过此注解spring容器会创建`AsyncConfigurationSelector`实例并调用其`selectImports`方法，完成异步调用相关的配置；

这里，我们了解到`@Enable***`注解控制某个模块的启用，实际上就是通过`@Import`注解引用Selector实例，并调用`selectImports`方法来装载指定的`BeanDefinition`；这种方法值得学习，在业务开发中也能用类似方式来对bean实例做控制

**常见的四种Import注解用法列举**
在`@Import`注解的参数中可以填写类名，例如`@Import(Abc.class)`，根据类Abc的不同类型，spring容器有以下四种处理方式：

1. 如果Abc类实现了`ImportSelector`接口，spring容器就会实例化Abc类，并且调用其`selectImports`方法，其会返回需要加载的Bean类，交由`ConfigurationClassPostProcessor`处理生成ConfigurationClass对象，最后通过this.reader.loadBeanDefinitions()变成BeanDefinition，再放入到BeanDefinitionMap中
2. `DeferredImportSelector`是`ImportSelector`的子类，如果Abc类实现了`DeferredImportSelector`接口，spring容器就会实例化Abc类，并且调用其`selectImports`方法，和`ImportSelector`的实例不同的是，`DeferredImportSelector`的实例的`selectImports`方法调用时机晚于`ImportSelector`的实例，要等到`@Configuration`注解中相关的业务全部都处理完了才会调用（具体逻辑在`ConfigurationClassParser.processDeferredImportSelectors`方法中）
3. 如果Abc类实现了`ImportBeanDefinitionRegistrar`接口，spring容器就会实例化Abc类，并且调用其`registerBeanDefinitions`方法；
4. 如果Abc没有实现`ImportSelector`、`DeferredImportSelector`、`ImportBeanDefinitionRegistrar`等其中的任何一个，spring容器就会实例化Abc类

实现步骤：

```java
基于ImportSelector导入指定Bean
1.构建一个自定义ImportSelector类，实现ImportSelector的selectImports方法（方法中需返回需要加载的Bean）
2.定义一个服务类，并将该类的全限定名作为1中selectImports方法的返回值（因为我们是通过ImportSelector来导入Bean，因此这个服务类无需通过注解声明为Bean加载对象）
3.构建一个配置类，配置类上声明@Configuration注解，标注其为配置类；同时，利用@Import注解，并指定1中构建的ImportSelect类。（这样，在ConfigurationClassPostProcessor解析配置类时，就会扫描到@Import注解中的ImportSelector，获取到需要加载的Bean，并将其转化为ConfigurationClass对象，通过this.reader.loadBeanDefinitions(configClasses);生成BeanDefinition后，注册到beanFactory）
```

```java
基于DeferredImportSelector导入指定Bean
实现逻辑同上一致，只是实现接口不一样，需要实现DeferredImportSelector的selectImports方法；同时，其执行阶段也晚于ImportSelector，在parse的最后一步执行
```

```java
基于ImportBeanDefinitionRegistrar注册BeanDefinition
1.构建一个自定义ImportBeanDefinitionRegistrar，实现ImportBeanDefinitionRegistrar的registerBeanDefinitions方法（方法中主要有两种注册BeanDefinition的策略）
    1.1 手动构建一个GenericBeanDefinition，并设置其属性信息等，然后通过BeanDefinitionRegistry来注册该BeanDefinition
    1.2 通过ClassPathBeanDefinitionScanner来扫描执行包下的Bean；同时，该scanner可以通过setIncludeFilter设置自定义的过滤器实现，来达到扫描指定注解或指定类型的Bean；还可以通过setBeanNameGenerator设置注册的BeanDefinition的命名策略
2.定义一个注解类，方便上述scanner设置的自定义过滤器扫描该类型注解进行类的过滤
3.定义一个服务类，服务类上声明自定义注解
4.构建一个配置类，配置类上声明@Configuration注解，标注其为配置类；同时，声明一个@ComponentScan注解，注解中指定扫描的包（主要是为了在1.2中，获取配置类上的注解信息，从而得知扫描包的路径。当然了，声明别的自定义的注解也行，只要注解中包含扫描包的路径即可）；最后，还需声明一个@Import注解，指定通过自定义ImportBeanDefinitionRegistrar来完成beanDefinition的装载。

在parse解析完成，将需要加载的Bean转化成ConfigurationClass对象后，通过this.reader.loadBeanDefinitions()来将ConfigurationClass对象转化成BeanDefinition并注册到BeanFactory后，自定义的ImportBeanDefinitionRegistrar会调用registerBeanDefinitions注册beanDefinition
也就是说触发顺序为：ImportSelector  》》 DeferredImportSelector  》》  ImportBeanDefinitionRegistrar
```

**ImportSelector和ImportBeanDefinitionRegistrar使用区别**

ImportSelector是通过实现selectImport直接返回需要导入的类的全限定名，并生成一个configurationClass对象（缓存了导入类的信息）。然后在loadBeanDefinition步骤时注册到BeanFactory中，此步骤早于ImportBeanDefinitionRegistrar的触发。且该方法的使用比较局限，必须指定正确完整的全限定名，如果修改后，该返回值也需修改。



ImportBeanDefinitionRegistrar则是调用registerBeanDefinitions直接注册beanDefinition，其是在ConfigurationClassPostProcessor解析@Import注解中的selector时，匹配到ImportBeanDefinitionRegistrar类型的话，则直接将该ImportBeanDefinitionRegistrar缓存到当前的configurationClass对象中，等到loadBeanDefinition步骤时触发ImportBeanDefinitionRegistrar的registerBeanDefinitions来实现beanDefinition的注册；

且registerBeanDefinitions的逻辑需要手动调用registry.registerBeanDefinition来注册到beanFactory，其实现可以通过两种方式：手动创建BeanDefinition（需要手动指定类的全限定名，和ImportSelector一样，遇到修改的话，也需要修改）、通过scanner扫描指定条件的Bean（这种实现可以指定过滤器，通过过滤指定的注解来扫描需要的bean，这样的话就可以自定义一个注解，然后在需要导入的Bean类上声明该注解，即可实现这些BeanDefinition的注册）



ImportSelector和ImportBeanDefinitionRegistrar均为spring的自动装配提供了支持，可以定义一个@Enable***的注解，在这个注解上使用@Import注解，通过这个注解来导入一个配置类，这个配置类中亦可以通过@Import注解导入多个selector来导入需要的BeanDefinition，这样就通过一个Enable注解导入了对应模块用到的BeanDefinition，后续在使用时，就能从BeanFactory中获取到定义文件进行实例化了。

具体实现见代码模块

[springboot-autoConfiguration-import](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-autoConfiguration-import)

完成上述@Import导入Bean的几种实现后，就可以通过自定义一个`@Enable***`注解，声明@Import注解引用配置类，来实现通过`@Enable***`装载指定Bean对象，开启模块功能的作用；

实现步骤

```java
在完成了上述ImportSelector的实现后
1.定义一个注解@EnableCustom，并在注解类上通过@Import注解引用上述过程定义的配置类
2.基于maven打包（mvn install）
3.创建一个新的模块应用，并在pom.xml文件中引用2中打包的依赖
4.在启动类上声明注解@EnableCustom，并创建一个service服务类，并自动注入一个上述ImportSelector中导入的bean
5.测试调用是否能调用打包的starter中的bean对象
```

###### 6.this.reader.loadBeanDefinitions(configClasses)

在parse()解析完成后，由`this.reader.loadBeanDefinitions(configClasses)`将parse过程解析装载到ConfigClasses中的配置类转化成BeanDefinition（包含@Import、@Bean注解声明的配置类），并加载到**BeanDefinitionMap**中

```java
public void loadBeanDefinitions(Set<ConfigurationClass> configurationModel) {
    TrackedConditionEvaluator trackedConditionEvaluator = new TrackedConditionEvaluator();
    for (ConfigurationClass configClass : configurationModel) {
    // 循环调用loadBeanDefinitionsForConfigurationClass()
        loadBeanDefinitionsForConfigurationClass(configClass, trackedConditionEvaluator);
    }
}

private void loadBeanDefinitionsForConfigurationClass(
        ConfigurationClass configClass, TrackedConditionEvaluator trackedConditionEvaluator) {
    // 省略部分代码 ... 

    // 如果一个bean是通过@Import(ImportSelector)的方式添加到容器中的，那么此时configClass.isImported()返回的是true
    // 而且configClass的importedBy属性里面存储的是ConfigurationClass,就是即将导入的bean类
    // 这一步的目的是为了根据BeanFactory中设置的命名策略对导入的Bean类进行命名，并注册到beanDefinitionMap中
    if (configClass.isImported()) {
        registerBeanDefinitionForImportedConfigurationClass(configClass);
    }
    // 判断当前的bean中是否含有@Bean注解的方法，如果有，需要把这些方法产生的bean放入到BeanDefinitionMap当中
    // 可能加载的配置类中，存在使用@Bean创建bean对象的方式，会被解析成configurationClass，然后在此处加载到beanDefinitionMap中
    for (BeanMethod beanMethod : configClass.getBeanMethods()) {
        loadBeanDefinitionsForBeanMethod(beanMethod);
    }
    loadBeanDefinitionsFromImportedResources(configClass.getImportedResources());
    // 如果bean上存在@Import注解，且import的是一个实现了ImportBeanDefinitionRegistrar接口,则执行ImportBeanDefinitionRegistrar的registerBeanDefinitions()方法
    loadBeanDefinitionsFromRegistrars(configClass.getImportBeanDefinitionRegistrars());
}
```

注册由importSelector导入的ConfigurationClass（Bean类）到BeanFactory的beanDefinitionMap中

```java
private void registerBeanDefinitionForImportedConfigurationClass(ConfigurationClass configClass) {
   // 获取ConfigurationClass的元数据，包含导入类的全限定名等
   AnnotationMetadata metadata = configClass.getMetadata();
   AnnotatedGenericBeanDefinition configBeanDef = new AnnotatedGenericBeanDefinition(metadata);

   ScopeMetadata scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(configBeanDef);
   configBeanDef.setScope(scopeMetadata.getScopeName());
   // 通过registry(BeanFactory)的命名策略针对该ConfigurationClass(Bean类)生成其bean的名称
   String configBeanName = this.importBeanNameGenerator.generateBeanName(configBeanDef, this.registry);
   AnnotationConfigUtils.processCommonDefinitionAnnotations(configBeanDef, metadata);

   BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(configBeanDef, configBeanName);
   definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
   // 注册该configurationClass(Bean类)到beandefinitionMap中
   this.registry.registerBeanDefinition(definitionHolder.getBeanName(), definitionHolder.getBeanDefinition());
   configClass.setBeanName(configBeanName);

   if (logger.isTraceEnabled()) {
      logger.trace("Registered bean definition for imported class '" + configBeanName + "'");
   }
}
```

至此就以`ConfigurationClassPostProcessor`为例完成了`BeanDefinitionRegistryPostProcessor`的一轮对于beandefinition的新增修改操作，后续还有`BeanDefinitionRegistryPostProcessor`的操作与此一致，不再重复阐述了

###### 7.自定义BeanDefinitionRegistryPostProcessor

`BeanDefinitionRegistryPostProcessor`是spring提供的一个扩展点，可以在bean实例化前，对beanDefinition的属性进行修改，并具备注册新的beanDefinition至beanFactory的能力；但是，通常情况下，需要新增beanDefinition的话，通过上述的`ConfigurationClassPostProcessor`已经可以完美实现，且符合大众的使用，因此个人不推荐再去重复造轮子。至于修改beanDefinition，在下面的`BeanFactoryPostProcessor`中也可以实现，没必要在这里做这件事

------

#### 5.3 触发BeanFactoryPostProcessor修改BeanDefinition

在筛选执行完`BeanDefinitionRegistryPostProcessor`类型的处理器后，会继续将`BeanFactoryPostProcessor`类型的处理器过滤出来，并通过`@PriorityOrdered`、`@Ordered`注解将其分别放入三个队列中缓存，按序调用处理器的`postProcessBeanFactory`方法

```java
// Do not initialize FactoryBeans here: We need to leave all regular beans
// uninitialized to let the bean factory post-processors apply to them!
// 在bean实例化前，修改beanDefinition的属性
// 获取BeanFactoryPostProcessor类型的后置处理器
String[] postProcessorNames =
      beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
// Ordered, and the rest.
// 分别获取实现了PriorityOrdered、Ordered，以及剩余的BeanFactoryPostProcessors，并装入对应的集合中
List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
List<String> orderedPostProcessorNames = new ArrayList<>();
List<String> nonOrderedPostProcessorNames = new ArrayList<>();
for (String ppName : postProcessorNames) {
   if (processedBeans.contains(ppName)) {
      // skip - already processed in first phase above
   }
   else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
      priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
   }
   else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
      orderedPostProcessorNames.add(ppName);
   }
   else {
      nonOrderedPostProcessorNames.add(ppName);
   }
}

// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
// 根据PriorityOrdered的实现进行排序，然后触发beanFactory的后置处理
sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
// 根据Ordered的实现进行排序，然后触发beanFactory的后置处理
List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
for (String postProcessorName : orderedPostProcessorNames) {
   orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
}
sortPostProcessors(orderedPostProcessors, beanFactory);
invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

// Finally, invoke all other BeanFactoryPostProcessors.java
List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
for (String postProcessorName : nonOrderedPostProcessorNames) {
    // 获取BeanFactory中注册的类型为BeanFactoryPostProcessors的BeanDefinition，并提前实例化，方便通过该BeanFactoryPostProcessor修改其他的注册了的BeanDefinitions
   nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
}
invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

// Clear cached merged bean definitions since the post-processors might have
// modified the original metadata, e.g. replacing placeholders in values...
beanFactory.clearMetadataCache();
```

从`beanFactory`中获取`BeanFactoryPostProcessor`类型的bean时，同样能获取到`ConfigurationClassPostProcessor`，因为其实现的接口`BeanFactoryRegistryPostProcessor`继承了接口`BeanFactoryPostProcessor`

![image-20220119164813259](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220119164813259.png)

因此，这里继续从它来分析后续的调用过程

```java
private static void invokeBeanFactoryPostProcessors(
      Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

   for (BeanFactoryPostProcessor postProcessor : postProcessors) {
      postProcessor.postProcessBeanFactory(beanFactory);
   }
}

// ConfigurationClassPostProcessor实现
public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    int factoryId = System.identityHashCode(beanFactory);
    if (this.factoriesPostProcessed.contains(factoryId)) {
        throw new IllegalStateException(
            "postProcessBeanFactory already called on this post-processor against " + beanFactory);
    }
    
    // 下面的if语句不会进入，因为在执行BeanFactoryPostProcessor时，会先执行BeanDefinitionRegistryPostProcessor的postProcessorBeanDefinitionRegistry()方法
    // 而在执行postProcessorBeanDefinitionRegistry方法时，都会调用processConfigBeanDefinitions方法，这与postProcessorBeanFactory()方法的执行逻辑是一样的
    // postProcessorBeanFactory()方法也会调用processConfigBeanDefinitions方法，为了避免重复执行，所以在执行方法之前会先生成一个id，将id放入到一个set当中
    // 先判断id是否存在，所以在此处，永远不会进入到if语句中
    this.factoriesPostProcessed.add(factoryId);
    if (!this.registriesPostProcessed.contains(factoryId)) {
        // BeanDefinitionRegistryPostProcessor hook apparently not supported...
        // Simply call processConfigurationClasses lazily at this point then.
        processConfigBeanDefinitions((BeanDefinitionRegistry) beanFactory);
    }

    // 对加了@Configuration注解的配置类进行Cglib代理
    enhanceConfigurationClasses(beanFactory);
    // 添加一个BeanPostProcessor后置处理器
    beanFactory.addBeanPostProcessor(new ImportAwareBeanPostProcessor(beanFactory));
}
```

##### 5.3.1 CGLib增强@Configuration

首先，在分析源码前，针对**为何要使用CGLib对@Configuration声明的注解类进行增强**的问题给出一个结论：通过CGLib增强`@Configuration`可以保证bean在工厂中以单例的形式存在

接下来针对这个结论结合源码进行分析：

跟进`enhanceConfigurationClasses(beanFactory)`

```java
public void enhanceConfigurationClasses(ConfigurableListableBeanFactory beanFactory) {
		Map<String, AbstractBeanDefinition> configBeanDefs = new LinkedHashMap<>();
		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
			// 判断一个类是否是一个全注解类
			// 扫描是全注解类？full和lite的关系（@Configuration声明的配置类为full）
			if (ConfigurationClassUtils.isFullConfigurationClass(beanDef)) {
				if (!(beanDef instanceof AbstractBeanDefinition)) {
					throw new BeanDefinitionStoreException("Cannot enhance @Configuration bean definition '" + beanName + "' since it is not stored in an AbstractBeanDefinition subclass");
				}
				else if (logger.isWarnEnabled() && beanFactory.containsSingleton(beanName)) {
					logger.warn("Cannot enhance @Configuration bean definition '" + beanName +
							"' since its singleton instance has been created too early. The typical cause " + "is a non-static @Bean method with a BeanDefinitionRegistryPostProcessor " + "return type: Consider declaring such methods as 'static'.");
				}
				configBeanDefs.put(beanName, (AbstractBeanDefinition) beanDef);
			}
		}
		if (configBeanDefs.isEmpty()) {
			// nothing to enhance -> return immediately
			return;
		}

		ConfigurationClassEnhancer enhancer = new ConfigurationClassEnhancer();
		for (Map.Entry<String, AbstractBeanDefinition> entry : configBeanDefs.entrySet()) {
			AbstractBeanDefinition beanDef = entry.getValue();
			// If a @Configuration class gets proxied, always proxy the target class
			beanDef.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
			try {
				// Set enhanced subclass of the user-specified bean class
				Class<?> configClass = beanDef.resolveBeanClass(this.beanClassLoader);
				if (configClass != null) {
					// 完成对全注解类的cglib代理
					Class<?> enhancedClass = enhancer.enhance(configClass, this.beanClassLoader);
					if (configClass != enhancedClass) {
						if (logger.isDebugEnabled()) {
							logger.debug(String.format("Replacing bean definition '%s' existing class '%s' with " + "enhanced class '%s'", entry.getKey(), configClass.getName(), enhancedClass.getName()));
						}
						beanDef.setBeanClass(enhancedClass);
					}
				}
			}
			catch (Throwable ex) {
				throw new IllegalStateException("Cannot load configuration class: " + beanDef.getBeanClassName(), ex);
			}
		}
	}

```

通过上述代码可以看到，cglib代理只会针对由@Configuration声明的Full类型(@Configuration)的配置类进行加强，lite类型(@Component、@Bean等等)不做任何处理

继续跟进`enhancer.enhance(configClass, this.beanClassLoader)`

```java
public Class<?> enhance(Class<?> configClass, @Nullable ClassLoader classLoader) {
    // 判断是否被代理过
    if (EnhancedConfiguration.class.isAssignableFrom(configClass)) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Ignoring request to enhance %s as it has " +
                                       "already been enhanced. This usually indicates that more than one " +
                                       "ConfigurationClassPostProcessor has been registered (e.g. via " +
                                       "<context:annotation-config>). This is harmless, but you may " +
                                       "want check your configuration and remove one CCPP if possible",
                                       configClass.getName()));
        }
        return configClass;
    }
    // 如果没有被代理就cglib代理
    Class<?> enhancedClass = createClass(newEnhancer(configClass, classLoader));
    if (logger.isDebugEnabled()) {
        logger.debug(String.format("Successfully enhanced %s; enhanced class name is: %s",
                                   configClass.getName(), enhancedClass.getName()));
    }
    return enhancedClass;
}
```

这里判断是否被代理过其实就看有没有实现`EnhancedConfiguration`这个接口。如果有就直接返回代理，否则创建代理。

```java
private Enhancer newEnhancer(Class<?> configSuperClass, @Nullable ClassLoader classLoader) {
    Enhancer enhancer = new Enhancer();
    // 增强父类，cglib是基于继承来的
    enhancer.setSuperclass(configSuperClass);
    // 增强接口，为什么要增强接口
    // 便于判断，表示一个类已经被增强了
    enhancer.setInterfaces(new Class<?>[] {EnhancedConfiguration.class});
    enhancer.setUseFactory(false);
    enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
    /**
	 * BeanFactoryAwareGeneratorStrategy是一个生成策略
	 * 主要是为生成的cglib类中添加成员变量$$beanFactory
	 * 同时基于接口EnhancedConfiguration的父接口BeanFactoryAware中的setBeanFactory方法，
	 * 设置此变量的值为当前context中的beanFactory，这样一来我们这个cglib代对象就有了beanFactory
	 * 有了beanFactory就能获得对象，而不用去通过方法获得对象了，因为通过放法获得对象不能控制过程
	 * 该beanFactory的作用是在this调用的拦截该调用，并直接在beanFactory中获得目标bean
     */
    enhancer.setStrategy(new BeanFactoryAwareGeneratorStrategy(classLoader));
    // 设置过滤方法，不能每次都去new
    enhancer.setCallbackFilter(CALLBACK_FILTER);
    enhancer.setCallbackTypes(CALLBACK_FILTER.getCallbackTypes());
    return enhancer;
}
```

cglib的代理是基于继承来的，这里设置了父类，也就是`Appconfig`，然后设置接口`EnhancedConfiguration`，这也就是刚为什么是可以通过判断是否由这个接口来断定是否被代理过了。

然后，我们查看一下这个`CALLBACK_FILTER`，其中包含了两个拦截器`BeanMethodInterceptor`和`BeanFactoryAwareMethodInterceptor`

![image-20220125104014769](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220125104014769.png)

`BeanFactoryAwareMethodInterceptor`在`ImportAwareBeanPostProcessor`后置处理器调用`setBeanFactory`填充时会将BeanFactory注入属性“$$beanFactory”，一方面`BeanMethodInterceptor`这个拦截器可以在访问`@Bean`注解的方法的时候，保证生成的bean是单例的，具体就是在调用方法时，拦截器中会根据`BeanMethod`对应的`BeanName`首先在`BeanFactory`中进行查找，若存在则直接返回工厂中的bean

------

在调用完成所有`BeanFactoryPostProcessor`后，就会清理元数据缓存

```java
// Clear cached merged bean definitions since the post-processors might have
// modified the original metadata, e.g. replacing placeholders in values...
beanFactory.clearMetadataCache();
```

至此，BeanFactory后置处理的相关源码已经扩展点已全部讲解完，继续分析下一步流程`registerBeanPostProcessors`

------

### 6）registerBeanPostProcessors注册Bean初始化的后置处理

##### 6.1 源码解析

本方法会注册所有的 `BeanPostProcessor`，将所有实现了 `BeanPostProcessor` 接口的类加载到 `BeanFactory` 中，并且提前实例化这些BeanPostProcessor.

`BeanPostProcessor` 接口是 Spring 初始化 bean 时对外暴露的扩展点，Spring IoC 容器允许 `BeanPostProcessor` 在容器初始化 bean 的前后，添加自己的逻辑处理，即对于Bean的创建和销毁过程中的生命周期进行管理。**在 registerBeanPostProcessors 方法只是注册到 BeanFactory 中，具体调用是在 bean 初始化的时候。**
具体执行步骤：在所有 bean 实例化时，执行初始化方法前会调用所有 `BeanPostProcessor` 的 **postProcessBeforeInitialization** 方法，在执行初始化方法后会调用所有 `BeanPostProcessor` 的 **postProcessAfterInitialization** 方法。

```java
public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {
		// 获取所有的 BeanPostProcessor
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// 记录信息，即当前beanFactory有几个beanpostProcessor，而最终要有多少个。
		// +1 的原因是加上 BeanPostProcessorChecker 类型BeanPostProcessor
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		// BeanPostProcessorChecker 主要是检查数量
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// 将实现PriorityOrdered的接口放入priorityOrderedPostProcessors中，Ordered 放入 orderedPostProcessorNames，其他放入 nonOrderedPostProcessorNames中。
		// 另外，如果既是PriorityOrdered又是MergedBeanDefinitionPostProcessor，则放入internalPostProcessors
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                // 提前实例化BeanPostProcessor
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// 将所有 priorityOrderedPostProcessors 注入beanFacotory.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// 将所有orderedPostProcessors 注入beanFactory，并将MergedBeanDefinitionPostProcessor 放入internalPostProcessors 中.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// 将所有其他的 nonOrderedPostProcessors 注入beanFactory，并将MergedBeanDefinitionPostProcessor 放入internalPostProcessors 中. 
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// 重新再将 internalPostProcessors 注入beanFactory中.
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		//  添加 ApplicationListenerDetector 到beanFactory中Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}
```

上面逻辑主要是区分 不同类型的 `BeanPostProcessors`，并按照 **PriorityOrdered -> Ordered -> 其他** 顺序放入beanFactory中。
而最后，将 `MergedBeanDefinitionPostProcessor` 类型节点，**重新放到后面**。而末尾放置的 `ApplicationListenerDetector` 主要是将 属于 `ApplicationListener` 类型bean注册进 `ApplicationEventMulticaster`，以便全局事件通知。

------

##### 6.2 自定义实现BeanPostProcessor

实现步骤

```
通过BeanPostProcessor对Bean生命周期进行管理进行控制
 * Bean的实例化生命周期如下：
 *  实例化 Instantiation
 *  属性赋值 Populate
 *  初始化 Initialization
 *  销毁 Destruction
 * 实现步骤：
 *  1.定义一个服务提供Bean:CustomService，并通过@Service声明为Bean
 *  2.创建一个自定义BeanPostProcessor，并实现BeanPostProcessor，其声明了两个操作postProcessBeforeInitialization和postProcessAfterInitialization
 *      这两个操作对应了声明周期中初始化前后的过程，开发人员可以通过实现这两个方法来对Bean的初始化前后进行控制
 *  2.1 开发人员亦可以实现BeanPostProcessor的子接口InstantiationAwareBeanPostProcessor，其在前者的基础上增加了对于属性赋值过程的控制postProcessProperties
 *      通过postProcessProperties可以对属性赋值时修改赋值的内容
 *  2.2 亦可以通过实现InstantiationAwareBeanPostProcessor的子接口SmartInstantiationAwareBeanPostProcessor
 *      其在前者的基础上还新增了对于Bean构造函数调用的控制等
 *  3.上述创建的BeanPostProcessor需注册到IOC工厂，因此可以通过@Component注解的方式声明为Bean对象；也可以在spring.factories中指定BeanPostProcessors为当前实现；
 *      亦可以通过配置类的形式通过@Bean或者@Import导入该PostProcessor；
 *      若需要通过start提供给其他应用使用，可以在spring.factories中定义EnableAutoConfiguration为当前配置类，再通过配置注册postProcessor
```

具体代码实现见模块

[springboot-bean-postProcessor](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-bean-postProcessor)

需要注意的是，这一步只是将BeanPostProcessor的Bean对象提前实例化并装载到了BeanFactory中，其会在后续onRefresh阶段对BeanFactory中的所有Bean的实例化过程产生影响，因此，用户在构建BeanPostProcessor时，需将这一因素考虑进去，避免对非必要的Bean创建造成影响。

##### 6.3 AOP实现(基于BeanPostProcessor生成代理类)

在面向对象开发的过程中，常常会遇到一些非继承关系的对象需要添加一些公共的方法的情况，例如：日志记录、性能监控、事务处理等。在面向对象编程的过程中，需要在每个对象中添加重复的方法，这样就产生了大量的冗余代码，不利于维护。针对这个问题，spring提出了AOP面向切面编程的方式来处理这一问题。简单来说，就是将重复的操作交由“切面”进行统一的处理，将代码集中到切面类一处，不仅提高了代码的可维护性，还极大的减少的冗余的代码量。

**AOP核心概念**

> **通知**（Advice）切面在某个连接点执行的操作(分为: Before advice,After returning advice, After throwing advice,After (finally) advice,Around advice )，通俗来说，就是在拦截的连接点前后执行统一的处理
>
> **连接点**（Join Point）连接点,也就是可以进行横向切入的位置，即需要进行拦截的方法
>
> **切点**（Poincut）是定义了在“什么地方”进行切入，哪些连接点会得到通知，包含一到多个连接点。显然，切点一定是连接点。
>
> **切面**（Aspect）是通知和切点的结合。通知和切点共同定义了切面的全部内容——是什么，何时，何地完成功能。
>
> **引入**（Introduction）允许我们向现有的类中添加新方法或者属性。
>
> **织入**（Weaving）是把切面应用到目标对象并创建新的代理对象的过程，分为编译期织入、类加载期织入和运行期织入。

###### AOP使用

1.引入依赖

在spring-boot-starter-aop的spring.factories定义中配置了EnableAutoConfiguration的值为AopConfiguration，通过这个配置类来装载对应的beanDefinition到BeanFacotory中，为后续实例化AOP相关的Bean到IOC容器中做准备

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

2.定义拦截的连接点

```java
@Service
public class CustomServiceImpl implements ICustomService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomServiceImpl.class);
    @Override
    public void test() {
        logger.info("invoke customService.method[test()]");
    }
}
```

3.定义并实现切面类

步骤如下：

```
1.创建自定义切面类CustomAspect，并使用@Aspect注解声明该类
2.声明切面类后还需将该切面类装载到IOC容器中，否则拦截时就加载不到该切面了，因此，还需要通过@Component注解声明为Bean对象；亦可以通过配置类的形式通过@Bean或者@Import导入该Bean对象；若需要通过start提供给其他应用使用，可以在spring.factories中定义EnableAutoConfiguration为当前配置类，再通过配置注册Bean对象
3.采用@PointCut声明切点，支持execution、within、this、target、以及args表达式
4.定义通知Advice实现，包含@Before前置处理、@After后置处理、@Around环绕处理（唯一支持获取入参）、@AfterReturning返回处理、以及@AfterThrowing异常后置处理
```

```java
@Aspect
@Component
public class CustomAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomAspect.class);
    
    @Pointcut(value = "execution(* cn.com.shuchang.springboot.study.service.impl.*.*(..))")
    public void pointCut(){}
    
    @Before("pointCut()")
    public void beforeMethod(){
        logger.info("invoke aspect's beforeMethod");
    }

    @After("pointCut()")
    public void afterMethod(){
        logger.info("invoke aspect's afterMethod");
    
    
}
```

4.启动测试

![image-20220718163903147](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220718163903147.png)

**@DeclareParents使用**

> @DeclareParents主要是为被代理类添加一个接口的所有方法实现，使得被代理类在未实现指定接口时，也能调用接口方法

使用步骤如下：

在已经引用aop依赖的前提下，创建一个新的被代理类CustomServiceImpl2

```java
@Service
public class CustomServiceImpl2 implements ICustomService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomServiceImpl.class);
    @Override
    public void test() {
        logger.info("invoke customServiceImpl2.method[test()]");
    }
}
```

定义一个希望供代理类调用的接口IDeclareParent，以及其实现DeclareParentImpl

```java
public interface IDeclareParent {
    
    public void commonMethod();
}

public class DeclareParentImpl implements IDeclareParent {
    
    private static final Logger logger = LoggerFactory.getLogger(DeclareParentImpl.class);
    
    @Override
    public void commonMethod() {
        logger.info("this is IDeclareParent‘s method");
    }
}
```

创建一个切面类，并通过@DeclareParents声明一个变量

```java
@Aspect
@Component
public class CustomAspect2 {
    
    // @DeclareParents定义拦截的代理类，然后通过defaultImpl指定增强的实现类
    @DeclareParents(value = "cn.com.shuchang.springboot.study.service.impl.CustomServiceImpl2", defaultImpl = DeclareParentImpl.class)
    public IDeclareParent declareParent;
}
```

启动类中通过上下文获取被代理类CustomServiceImpl2，并强转未IDeclareParent类型，调用commonMethod

```java
public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AspectBeanPostProcessorApplication.class, args);
        ICustomService service = (ICustomService)context.getBean("customServiceImpl");
        service.test();
        // 验证@DeclareParents
        ICustomService service2 = (ICustomService)context.getBean("customServiceImpl2");
        service2.test();
        ((IDeclareParent)service2).commonMethod();
    }
```

![image-20220725150715765](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220725150715765.png)

具体代码实现见

[springboot-beanPostProcessor-aspect](https://github.com/shuchang1011/springbootStudy/tree/main/study-parent/springboot-beanPostProcessor-aspect)



###### AOP原理分析

在上述AOP使用中，在导入了spring-boot-starter-aop的依赖后，就开启了AOP切面的功能，那么AOP是如何装载到IOC容器中？又是如何针对切入点进行拦截处理呢？接下来，我们针对这两个问题进行分析。

**自动装配AOP**

在启动类上声明的注解`@SpringBootApplication`中，包含了`@EnableAutoConfiguration`注解，通过该注解会加载`AutoConfigurationImportSelector`来实现自动配置类的装载过程，这其中就包含了`AopAutoConfiguration`注解(该自动配置装载的过程见**5.2 自动装配原理(DeferredImportSelector实现)**)。

在AopConfiguration配置类中，主要定义了一下配置内容

```java
// 声明为配置类，并取读上下文环境中的spring.aop.auto的属性，默认为true
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "spring.aop", name = "auto", havingValue = "true", matchIfMissing = true)
public class AopAutoConfiguration {

    // classpath下存在org.aspectj.weaver.Advice时，激活配置类AspectJAutoProxyingConfiguration，其中声明了两个aop核心实现的配置类JdkDynamicAutoProxyConfiguration、CglibAutoProxyConfiguration
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(Advice.class)
	static class AspectJAutoProxyingConfiguration {

        // 获取上下文环境中的spring.aop.proxy-target-class的属性，默认为false，值为false时激活配置JdkDynamicAutoProxyConfiguration，然后加载到注解EnableAspectJAutoProxy，通过该注解上的Import注解引用AspectJAutoProxyRegistrar来注册默认的AdviceCreator：AnnotationAwareAspectJAutoProxyCreator，该类用于创建Bean的代理类，代理方式采用Jdk动态代理(需要声明接口)
		@Configuration(proxyBeanMethods = false)
		@EnableAspectJAutoProxy(proxyTargetClass = false)
		@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "false",
				matchIfMissing = false)
		static class JdkDynamicAutoProxyConfiguration {

		}

        // 获取上下文中的spring.aop.proxy-target-class的属性，默认为true，值为true时激活配置CglibAutoProxyConfiguration，然后加载到注解EnableAspectJAutoProxy，通上述jdk动态配置类一致，但是这里或传递proxyTargetClass参数为true的值到引用的AspectJAutoProxyRegistrar中，而注册的AdviceCreator(AnnotationAwareAspectJAutoProxyCreator)，会基于cglib方式创建bean的代理类(无需声明接口，通过字节码技术为一个类创建子类,并在子类中采用方法拦截的技术拦截所有父类方法的调用,顺势织入横切逻辑)
		@Configuration(proxyBeanMethods = false)
		@EnableAspectJAutoProxy(proxyTargetClass = true)
		@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true",
				matchIfMissing = true)
		static class CglibAutoProxyConfiguration {

		}

	}

    // 在classpath下不存在org.aspectj.weaver.Advice时，即未引用aspectjweaver依赖时，且在上下文环境中spring.aop.proxy-target-class为true（默认为true）时，激活下列配置ClassProxyingConfiguration
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingClass("org.aspectj.weaver.Advice")
	@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true",
			matchIfMissing = true)
	static class ClassProxyingConfiguration {

        // 注册cglib形式的AdviceCreator(AnnotationAwareAspectJAutoProxyCreator)到BeanFactory中
		ClassProxyingConfiguration(BeanFactory beanFactory) {
			if (beanFactory instanceof BeanDefinitionRegistry) {
				BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
				AopConfigUtils.registerAutoProxyCreatorIfNecessary(registry);
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
		}

	}

}
```

@EnableAspectJAutoProxy注解声明如下，通过该注解引用`AspectJAutoProxyRegistrar`来注入AdviceCreator的实现`AnnotationAwareAspectJAutoProxyCreator`到BeanFactory中

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AspectJAutoProxyRegistrar.class)
public @interface EnableAspectJAutoProxy {

	/**
	 * Indicate whether subclass-based (CGLIB) proxies are to be created as opposed
	 * to standard Java interface-based proxies. The default is {@code false}.
	 */
	boolean proxyTargetClass() default false;

	/**
	 * Indicate that the proxy should be exposed by the AOP framework as a {@code ThreadLocal}
	 * for retrieval via the {@link org.springframework.aop.framework.AopContext} class.
	 * Off by default, i.e. no guarantees that {@code AopContext} access will work.
	 * @since 4.3.1
	 */
	boolean exposeProxy() default false;

}
```

`AspectJAutoProxyRegistrar`实现了`ImportBeanDefinitionRegistrar`接口中的registerBeanDefinitions实现，在ConfigurationClassPostProcessor解析并装载BeanDefinition到beanFactory中的最后一步，触发registerBeanDefinitions将注册指定的BeanDefinition到BeanFactory中

```java
class AspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar {

	/**
	 * Register, escalate, and configure the AspectJ auto proxy creator based on the value
	 * of the @{@link EnableAspectJAutoProxy#proxyTargetClass()} attribute on the importing
	 * {@code @Configuration} class.
	 */
	@Override
	public void registerBeanDefinitions(
			AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        // 注册AnnotationAwareAspectJAutoProxyCreator到BeanFactory中
		AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);

        // 设置AnnotationAwareAspectJAutoProxyCreator的beanDefinition中的proxyTargetClass和exposeProxy的属性，这两个属性的值从AopConfiguration中定义的注解中获取
		AnnotationAttributes enableAspectJAutoProxy =
				AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);
		if (enableAspectJAutoProxy != null) {
			if (enableAspectJAutoProxy.getBoolean("proxyTargetClass")) {
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
			if (enableAspectJAutoProxy.getBoolean("exposeProxy")) {
				AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
			}
		}
	}

}
```

这里，我们主要看一下registerAspectJAnnotationAutoProxyCreatorIfNecessary的逻辑实现

```java
// org.springframework.aop.config.AopConfigUtils
public abstract class AopConfigUtils {
 	public static final String AUTO_PROXY_CREATOR_BEAN_NAME =
			"org.springframework.aop.config.internalAutoProxyCreator"; 
    
    static {
		// Set up the escalation list...
		APC_PRIORITY_LIST.add(InfrastructureAdvisorAutoProxyCreator.class);
		APC_PRIORITY_LIST.add(AspectJAwareAdvisorAutoProxyCreator.class);
		APC_PRIORITY_LIST.add(AnnotationAwareAspectJAutoProxyCreator.class);
	}
    ...
    public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(BeanDefinitionRegistry 		registry) {
        return registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry, null);
    }

    public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(
                BeanDefinitionRegistry registry, @Nullable Object source) {
        return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator.class, registry, 							source);
    }

    // 注册切面类核心模块AdviceCreator
    private static BeanDefinition registerOrEscalateApcAsRequired(
                Class<?> cls, BeanDefinitionRegistry registry, @Nullable Object source) {
        Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
        
		// 判断beanFactory中Bean名为org.springframework.aop.config.internalAutoProxyCreator的BeanDefinition(值为AnnotationAwareAspectJAutoProxyCreator的Bean)
        if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
            BeanDefinition apcDefinition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
            // 若非AnnotationAwareAspectJAutoProxyCreator类型的Bean，则判断其是否是APC_PRIORITY_LIST中的定义，并获取优先级(按照集合中索引大小比对优先级：AnnotationAwareAspectJAutoProxyCreator > AspectJAwareAdvisorAutoProxyCreator > InfrastructureAdvisorAutoProxyCreator)，优先级越高的类型则设置为AdviceCreator的Bean的定义文件的实现类
            if (!cls.getName().equals(apcDefinition.getBeanClassName())) {
                int currentPriority = findPriorityForClass(apcDefinition.getBeanClassName());
                int requiredPriority = findPriorityForClass(cls);
                if (currentPriority < requiredPriority) {
                    apcDefinition.setBeanClassName(cls.getName());
                }
            }
            return null;
        }

        // 注册AnnotationAwareAspectJAutoProxyCreator类型的BeanDefinition到BeanFactory，并设置order为最高级别,优先执行
        RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
        beanDefinition.setSource(source);
        beanDefinition.getPropertyValues().add("order", Ordered.HIGHEST_PRECEDENCE);
        beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);
        return beanDefinition;
    }
}
```

至此，aop的核心模块的实现Bean——`AnnotationAwareAspectJAutoProxyCreator`就已经装载到了BeanFactory，其类的实现关系如下图所示

![image-20220725103522565](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220725103522565.png)

上图可知`AnnotationAwareAspectJAutoProxyCreator`又是`BeanPostProcessor`的实现类 具体的实现方法是在`AbstractAutoProxyCreator`中,也就是说在生成Bean的代理类时，主要是通过AbstractAutoProxyCreator.postProcessBeforeInstantiation(),postProcessAfterInitialization(),两个方法来完成Bean代理类的构建

```java
public Object postProcessBeforeInstantiation(Class beanClass, String beanName) throws BeansException {
     //省略
 }

 @Override
public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) throws BeansException {
    if (bean != null) {
        //beanName；
        Object cacheKey = getCacheKey(bean.getClass(), beanName);
        //todo earalyProxy 是用来缓存提前暴露对象的缓存key
        if (!this.earlyProxyReferences.contains(cacheKey)) {
            //创建代理Bean
            return wrapIfNecessary(bean, beanName, cacheKey);
        }
    }
    return bean;
}
```

查看创建代理Bean的核心方法wrapIfNecessary

```java
protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
    //这里表示已经处理过
    if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
        return bean;
    }
    //这里表示无需增强
    if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
        return bean;
    }
    //@1.1 给定的bean类是否Advice,Pointcut,Advisor,AopInfrastructureBean等这些类不需要被代理，
    //@1.2 或是指定的bean不需要被代理
    if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
        this.advisedBeans.put(cacheKey, Boolean.FALSE);
        return bean;
    }
    //@1.3 获取当前类的增强Advisors集合，可能不存在
    Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
    // advisors集合不为空
    if (specificInterceptors != DO_NOT_PROXY) {
        this.advisedBeans.put(cacheKey, Boolean.TRUE);
        //@1.4 这里创建代理（创建代理有两种形式，一个是JDK动态代理，一个是Cglib增强字节码）
        // jdk动态代理实际上是拦截接口的方法执行，然后通过Advice增强在方法前后调用增强方法
        // Cglib直接在BeanDefinition实例化返回对象前，修改类的方法
        Object proxy = createProxy(
                bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
        this.proxyTypes.put(cacheKey, proxy.getClass());
        return proxy;
    }
    this.advisedBeans.put(cacheKey, Boolean.FALSE);
    return bean;
}
```

上述代码可知创建一个代理对象大致流程为:

- 判断是否是特殊的Bean的类型,以及是否跳过.
- 获取增强器
- 创建代理Bean

@1.1 **isInfrastructureClass**

```java
// 如果是Advice,Pointcut,Advisor,AopInfrastructureBean类型的bean就跳过
protected boolean isInfrastructureClass(Class<?> beanClass) {
    boolean retVal = Advice.class.isAssignableFrom(beanClass) ||
            Pointcut.class.isAssignableFrom(beanClass) ||
            Advisor.class.isAssignableFrom(beanClass) ||
            AopInfrastructureBean.class.isAssignableFrom(beanClass);
    return retVal;
}
```

@1.2 **shouldSkip**

```java
protected boolean shouldSkip(Class<?> beanClass, String beanName) {
   // @1.2.1 获取所有的的Advisor增强器
    List<Advisor> candidateAdvisors = findCandidateAdvisors();
    for (Advisor advisor : candidateAdvisors) {
        if (advisor instanceof AspectJPointcutAdvisor &&
                ((AspectJPointcutAdvisor) advisor).getAspectName().equals(beanName)) {
            return true;
        }
    }
    return super.shouldSkip(beanClass, beanName);
}
```

@1.2.1 **findCandidateAdvisors获取所有的的Advisor增强器(AnnotationAwareAspectJAutoProxyCreator实现)**

```java
protected List<Advisor> findCandidateAdvisors() {
    //@1.2.1.1这一步其实就是获取Spring配置文件中定义的Advisor
    //或者被@Bean注解标识的Advisor例如BeanFactoryCacheOperationSourceAdvisor
    //和事务相关的BeanFactoryTransactionAttributeSourceAdvisor都是这一步获取到
    List<Advisor> advisors = super.findCandidateAdvisors();
    //@1.2.1.2 解析 @Aspect 注解，并构建通知器合并到List
    advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
    return advisors;
}
```

@1.2.1.2 **buildAspectJAdvisor()构建Advisor**

这里主要执行了以下几步操作：

- 获取IOC容器中的所有BeanDefinition
- 遍历所有BeanDefinition，判断其是否是被代理类
- 获取切面类，并根据切面中Advice的实现构建Advisors增强，最后追加到Advisor的集合中

```java
// org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
public List<Advisor> buildAspectJAdvisors() {
    List<String> aspectNames = this.aspectBeanNames;
    if (aspectNames == null) {
        synchronized (this) {
            aspectNames = this.aspectBeanNames;
            if (aspectNames == null) {
                List<Advisor> advisors = new LinkedList<>();
                aspectNames = new LinkedList<>();
                //获取所有的beanName IOC定义的所有的Beanname
                String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                    this.beanFactory, Object.class, true, false);
                //循环找出增强
                for (String beanName : beanNames) {
                    // 判断是否为切面类
                    if (!isEligibleBean(beanName)) {
                        continue;
                        //获取对应的bean类型
                        Class<?> beanType = this.beanFactory.getType(beanName);
                        if (beanType == null) {
                            continue;
                        }
                        // 获取切面类型的Bean
                        if (this.advisorFactory.isAspect(beanType)) {
                            //存入到list中
                            aspectNames.add(beanName);
                            //封装切面元数据
                            AspectMetadata amd = new AspectMetadata(beanType, beanName);
                            if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
                                // 获取切面类工厂(包含了切面类上声明的注解)
                                MetadataAwareAspectInstanceFactory factory =
                                    new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
                                // @1.2.1.2.1 根据切面类的实现构建Advisor增强器
                                List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
                                //判断是否是单例
                                if (this.beanFactory.isSingleton(beanName)) {
                                    this.advisorsCache.put(beanName, classAdvisors);
                                }
                                else {
                                    this.aspectFactoryCache.put(beanName, factory);
                                }
                                advisors.addAll(classAdvisors);
                            }
                            else {
                                //.........
                            } } }
                    this.aspectBeanNames = aspectNames;
                    return advisors;
                }
            }
        }
        if (aspectNames.isEmpty()) {
            return Collections.emptyList();
        }
        List<Advisor> advisors = new LinkedList<>();
        for (String aspectName : aspectNames) {
            //这里获取单例的Advisor
            List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
            if (cachedAdvisors != null) {
                advisors.addAll(cachedAdvisors);
            }
            else {
                MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
                advisors.addAll(this.advisorFactory.getAdvisors(factory));
            }
        }
        return advisors;
    }
```

@1.2.1.2.1 **getAdvisors根据切面类的实现构建Advisor增强器**

```java
	public List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory) {
        // 根据切面类工厂获取当前切面类和Bean名称
		Class<?> aspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
		String aspectName = aspectInstanceFactory.getAspectMetadata().getAspectName();
		validate(aspectClass);

		// We need to wrap the MetadataAwareAspectInstanceFactory with a decorator
		// so that it will only instantiate once.
		MetadataAwareAspectInstanceFactory lazySingletonAspectInstanceFactory =
				new LazySingletonAspectInstanceFactoryDecorator(aspectInstanceFactory);

		List<Advisor> advisors = new ArrayList<>();
        // 获取切面类中@PointCut声明的方法
		for (Method method : getAdvisorMethods(aspectClass)) {
            // @1.2.1.2.1.1 根据@PointCut表达式生成对应代理类的增强Advisor
			Advisor advisor = getAdvisor(method, lazySingletonAspectInstanceFactory, advisors.size(), aspectName);
			if (advisor != null) {
				advisors.add(advisor);
			}
		}

		// If it's a per target aspect, emit the dummy instantiating aspect.
		if (!advisors.isEmpty() && lazySingletonAspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
			Advisor instantiationAdvisor = new SyntheticInstantiationAdvisor(lazySingletonAspectInstanceFactory);
			advisors.add(0, instantiationAdvisor);
		}

        // 获取切面类中@DeclareParents声明的变量，并对被代理类进行增强，将生成的增强Advisor缓存到集合中
		// Find introduction fields.
		for (Field field : aspectClass.getDeclaredFields()) {
			Advisor advisor = getDeclareParentsAdvisor(field);
			if (advisor != null) {
				advisors.add(advisor);
			}
		}

		return advisors;
	}

	// @1.2.1.2.1.1 根据@PointCut表达式生成对应代理类的增强Advisor
	public Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory 							aspectInstanceFactory,int declarationOrderInAspect, String aspectName) {

		validate(aspectInstanceFactory.getAspectMetadata().getAspectClass());

        // 获取切面类中@PointCut注解中的表达式
		AspectJExpressionPointcut expressionPointcut = getPointcut(
				candidateAdviceMethod, aspectInstanceFactory.getAspectMetadata().getAspectClass());
		if (expressionPointcut == null) {
			return null;
		}
		
        // 返回针对@PointCut中表达式中的代理类进行增强的Advisors
		return new InstantiationModelAwarePointcutAdvisorImpl(expressionPointcut, candidateAdviceMethod,
				this, aspectInstanceFactory, declarationOrderInAspect, aspectName);
	}
```

到这里就解析获取了所有的增强Advisors实现，在`AnnotationAwareAspectJAutoProxyCreator`拦截所有BeanDefinition初始化时，都会去匹配所有增强Advisors中拦截的代理类，然后根据指定的代理方式(jdk动态代理||Cglib增强字节码)生成代理bean



#### 7) initMessageSource初始化国际化资源

在注册完成BeanPostProcessor到BeanFactory中后，就会执行下一步，进行国际化资源的初始化，其主要是通过加载名为messageSource的Bean来进行资源国际化，若BeanFactory中不存在名为messageSource的资源的话，则构建一个默认的DelegatingMessageSource

```java
protected void initMessageSource() {
    ConfigurableListableBeanFactory beanFactory = getBeanFactory();

    // Bean 的名称必须要是 messageSource
    if (beanFactory.containsLocalBean(MESSAGE_SOURCE_BEAN_NAME)) {
        this.messageSource = beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME, MessageSource.class);
        // Make MessageSource aware of parent MessageSource.
        if (this.parent != null && this.messageSource instanceof HierarchicalMessageSource) {
            HierarchicalMessageSource hms = (HierarchicalMessageSource) this.messageSource;
            if (hms.getParentMessageSource() == null) {
                // Only set parent context as parent MessageSource if no parent MessageSource
                // registered already.
                hms.setParentMessageSource(getInternalParentMessageSource());
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Using MessageSource [" + this.messageSource + "]");
        }
    }
    else {
        // Use empty MessageSource to be able to accept getMessage calls.
        // 否则则使用默认的
        DelegatingMessageSource dms = new DelegatingMessageSource();
        dms.setParentMessageSource(getInternalParentMessageSource());
        this.messageSource = dms;
        beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
        if (logger.isTraceEnabled()) {
            logger.trace("No '" + MESSAGE_SOURCE_BEAN_NAME + "' bean, using [" + this.messageSource + "]");
        }
    }
}
```

##### 国际化使用

具体步骤如下：

1. 首先，创建国际化文件Resource bundles![image-20220725174906733](C:\Users\shuchang\AppData\Roaming\Typora\typora-user-images\image-20220725174906733.png)![image-20220725174948119](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220725174948119.png)![image-20220725175024586](https://raw.githubusercontent.com/shuchang1011/images/main/img/image-20220725175024586.png)

2. 声明messageSource的bean对象，这里我们通过配置类来加载bean**(需注意，必须定义Bean名称为messageSource**)

   ```java
   @Configuration
   public class MessageSourceConfiguration {
   
       @Bean("messageSource")
       public MessageSource getMessageSource() {
           ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
   
           messageSource.setDefaultEncoding("UTF-8");
           messageSource.addBasenames("message", "message_en");
   
           return messageSource;    
       
       }
   }
   ```

3. 测试结果

   ```java
   @SpringBootApplication
   public class MessageSourceApplication {
       
       private static final Logger logger = LoggerFactory.getLogger(MessageSourceApplication.class);
       
       public static void main(String[] args) {
           ConfigurableApplicationContext context = SpringApplication.run(MessageSourceApplication.class, args);
           MessageSource messageSource = (MessageSource) context.getBean("messageSource");
           String zhMessage = messageSource.getMessage("user.name", null, null, Locale.CHINA);
           String enMessage = messageSource.getMessage("user.name", null, null, Locale.ENGLISH);
           logger.info("zhMessage:{}",zhMessage);
           logger.info("enMessage:{}",enMessage);
       }
   }
   ```

   