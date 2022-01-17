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
3.由于入口类注解@SpringBootApplication种包含了@ComponentScan注解，因此，启动时，回基于默认的BeanNameGenerator去装载对应命名的Bean definition。同时，加载到配置类时，又会触发一次扫描，基于自定义的BeanNameGenerator再次装载一个符合自定义命名策略的Bean Definition。因此，自定义命名策略需要和默认的命名策略区分开，否则会导致命名冲突的问题出现
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
    //创建 BeanDefinitionLoader 
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
         //运行阶段需要加锁注册进beanDefinitionMap
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
         //如果仍处于启动注册阶段，直接注册进beanDefinitionMap
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

接下来我们，断点调试，跳过准备容器阶段，可以看到启动类的`BeanDefinition`已经成功加载到Ioc容器的`BeanDefinitionMap`中了

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

作用：扩展initPropertySources()主要是为了在准备刷新容器时，往Environment环境中添加一些自定义的属性；相对于在第二阶段-准备环境过程中，通过实现EnvironmentPostProcessors接口来说，二者作用一直，但是执行的时机不一样。

实现步骤：

```java
1.因为我们是以web应用的方式启动springboot，因此，可以继承其实现类AnnotationConfigServletWebApplicationContext，并重写重写initPropertySources
2.在initPropertySources方法中，往环境Environment中设置必要属性，启动测试是否生效
```

具体实现见代码模块

[springboot-context-initPropertySouces]()