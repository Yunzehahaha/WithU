package tech.yunze.withu.LocationStufff

class WorkManager constructor(var applicationContext: String) {

    var name ="wang yunze"


    companion object{
        var singletonHolder: SingletonHolder<WorkManager, String> =
            SingletonHolder(::WorkManager)

    }}