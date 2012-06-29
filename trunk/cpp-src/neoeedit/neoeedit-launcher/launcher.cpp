#include <Windows.h>
#include <jni.h>

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nShowCmd){
	char* szBuf = getenv("PATH");
	//printf("%s\n", szBuf);
	char* context=NULL;
	char* p1 = strtok_s (szBuf,";", &context);
	char* BUF2=new char[1024];
	char* target=new char[1024];
	target[0]=0;  
	while (p1 != NULL)
	{
		char* buf3 = BUF2;
		buf3[0]=0;
		strcat_s(buf3,1024,p1);
		strcat_s(buf3,1024,"\\java.exe");
		DWORD exist = GetFileAttributes(buf3);
		if (exist!=0xFFFFFFFF){
			strcpy_s(target ,1024, buf3);
			strcat_s(buf3,1024," <found>");
		}
		//printf ("%s\n",buf3);

		p1 = strtok_s (NULL, ";",&context);
	}

	int pos1=strlen(target)-strlen("\\java.exe");
	target[pos1]=0;
	strcat_s(target,1024,"\\..\\jre\\bin\\client\\jvm.dll");
	DWORD exist = GetFileAttributes(target);
	if (exist!=0xFFFFFFFF){
	}else{
		target[pos1]=0;
		strcat_s(target,1024,"\\client\\jvm.dll");
		exist = GetFileAttributes(target);
		if (exist!=0xFFFFFFFF){
		}else{
			target[0]=0;
			printf ("jvm.dll not found.\n");
		}
	}

	if (strlen(target)>0){
		printf ("found=%s\n",target);
	}

	char*fn=new char[1024];
	GetModuleFileName(NULL,fn,1024);
	char* last_backslash = strrchr(fn, '\\'); 
	if (last_backslash)
	{
		*last_backslash = '\0';
	}
	strcat_s(fn,1024,"\\neoeedit.jar");
	if(0xFFFFFFFF==GetFileAttributes(fn)){
		printf("cannot find %s\n",fn);
		return 1;
	}

	HINSTANCE _libInst=NULL;
	typedef jint (JNICALL CreateJavaVM_t)(JavaVM **pvm, void **penv, void *args);

	if ( (_libInst = LoadLibrary(target)) == NULL) {
		printf("Can't load JVM DLL");
		return 2;
	}

	JavaVMOption options[3];
	// java
	char* opt0=new char[1024];
	sprintf_s(opt0,1024,"-Djava.class.path=%s",fn);
	options[0].optionString = opt0;
	options[1].optionString ="-Xms64M";
	options[2].optionString ="-Xmx512M";
	JavaVMInitArgs vm_args;
	vm_args.version = JNI_VERSION_1_6;
	vm_args.options = options;
	vm_args.nOptions = 3;
	vm_args.ignoreUnrecognized = false;

	JNIEnv * x_env;
	JavaVM * x_jvm;
	jclass x_cls;
	CreateJavaVM_t* createFn = (CreateJavaVM_t *)GetProcAddress(_libInst, "JNI_CreateJavaVM");
	jint res = createFn(&x_jvm, (void**)&x_env, &vm_args);
	x_cls =  x_env ->FindClass("neoe/ne/Main");
	jmethodID mid = x_env->GetStaticMethodID(x_cls, "main","([Ljava/lang/String;)V");
	jobjectArray args;
	jstring jstr;
	jstr = (x_env)->NewStringUTF(lpCmdLine);
	jclass stringClass;

	stringClass = (x_env)->FindClass( "java/lang/String");
	if(lpCmdLine && strlen(lpCmdLine)>0){
		args = (x_env)->NewObjectArray( 1, stringClass, jstr);
	}else{
		args = (x_env)->NewObjectArray( 0, stringClass, NULL);
	}
	(x_env)->CallStaticVoidMethod(x_cls, mid, args);

	if ((x_env)->ExceptionOccurred()) {
		(x_env)->ExceptionDescribe();
	}
	(x_jvm)->DestroyJavaVM();     
	return 0;
}