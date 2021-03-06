//TODO:
// - Make connection non-reliant on every computer being present (DONE)
// - Integrate azimuth calculations using coordinates (DONE)
// - Tie coordinate data to a specific operator (DONE)
// - Figure out JavaSoundSampled panning tools (DONE)

/*lwjgl imports will require OpenAl64.dll and lwjgl64.dll to be added
 * to SDK bin directory (EX: Users\\<User Name>\\.jdks\\azul-15.0.3\\bin)
 *
 * Download Azul-15.0.3 at https://www.azul.com/downloads/?package=jdk#download-openjdk
 * Download lwjgl 2.9.3 from legacy.lwjgl.org/download.php.html
 *
 * These .dll files can be found in lwjgl-2.9.3\\native\\windows folder
 */

import org.lwjgl.openal.AL
import java.io.IOException
import SpatialAudio.SpatialAudioFun as SpatialAudio
import SpatialAudio.Help as Help

object Operator{

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {

        //Get host credentials
        SpatialAudio.getHost()

        //Set the initial ports for portAudio and Hyper IMU.
        //Note: initial port for portAudio should be set to the first port within a range of 8 ports
        // EG: if a range of ports 6011 -> 6018 are needed, then portAudio should be set to 6011.
        SpatialAudio.setInitPorts()

        /** Port on which strings are sent. Default: 8000
         */
        val portString = 8000

        /** Port on which the multicast server is created. Default: 8010
         */
        val portConnect = 8010

        try {
            //Create OpenAl instance
            AL.create()

            //Initialize Microphone
            SpatialAudio.initMic()

            //Initialize Port for handling sent Strings
            SpatialAudio.initStringPort(portString)

            // Handles the creation of the multicast network.
            SpatialAudio.initMulticast("230.0.0.0", portConnect)

            //Handles PTT initialization and listens for the specified key
            SpatialAudio.initPTT('t')

            //Handles DEMO MODE initialization and listens for the specified key
            SpatialAudio.initSADemo('a','d','w','s','x', 'n', 'm', 'r')


            //region THREADS: Contains all running threads.
            /**
             * This THREAD's primary purpose is to provide helpful troubleshooting information.
             */
            class HelpThread: Runnable {
                override fun run(){
                    Help.help()
                }
            }

            /**
             * This THREAD's primary purpose is to send requests over the multicast network to other
             * connected operators. This will allow operators to add self to their DATABASE.
             */
            //region ConnectThread: SEND OP REQUESTS OVER MULTICAST SERVER
            class ConnectThread: Runnable {
                override fun run() {
                    while (true) {
                        SpatialAudio.sendRequest(portConnect)
                    }
                }
            }
            //endregion

            /**
             * This THREAD's primary purpose to receive operators connected to the multicast network
             * and allocate the operators to their respective audio ports.
             */
            //region ConnectRecThread: RECEIVE OPERATOR REQUESTS
            class ConnectRecThread: Runnable{
                override fun run(){
                    while(true) {
                        SpatialAudio.receiveOP()
                    }
                }
            }
            //endregion

            /**
             * This THREAD's primary purpose is to send OWN GPS data over the multicast network to other connected
             * operators for use in their azimuth calculations.
             *
             * This THREAD will also determine when an operator disconnects based on the time since their last
             * response. Each response is sent/received every 1 second.
             */
            //region SendStringThread: SEND COORDINATES OVER MULTICAST SERVER
            class SendStringThread: Runnable {
                override fun run() {
                    while (true) {
                        SpatialAudio.sendData(portString)
                    }
                }
            }
            //endregion

            /**
             * This THREAD's primary purpose is to receive operators GPS data and allocate it to their respective
             * DATA CLASS
             */
            //region RecStringThread: RECEIVE OPERATOR COORDINATES
            class RecStringThread: Runnable {
                override fun run() {
                    while (true) {
                        SpatialAudio.receiveData()
                    }
                }
            }

            /**
             * This THREAD's primary purpose is to send own microphone audio over the multicast
             * network to other connected operators.
             */
            //region SendThread: Send TARGETDATALINE audio
            class SendThread: Runnable {
                override fun run() {
                    while (true) {
                        SpatialAudio.sendAudio()
                    }
                }
            }
            val threadSendThread = Thread(SendThread())
            //endregion

            /**
             * This THREAD's primary purpose is to enable push-to-talk and suspend/resume the send thread
             * to prevent sending microphone data when not pushed.
             */
            //region PTTThread: ACTIVATES PTT ELSE SUSPENDS SendThread
            class PTTThread: Runnable {
                override fun run() {
                    while (true){
                        SpatialAudio.suspendThread(threadSendThread)
                    }
                }
            }
            threadSendThread.start()
            //endregion


            /**
             * These THREAD's handle all received operators audio and processes it for Spatial Audio purposes.
             */
            //region RecThread: ALL RECEIVING THREADS FOR AUDIO
            // Receiving OP-1 audio
            class RecThread: Runnable{
                override fun run(){
                    SpatialAudio.recAudio("OP1")
                }
            }

            // Receiving OP-2 audio
            class RecThread2: Runnable{
                override fun run(){
                    SpatialAudio.recAudio("OP2")
                }
            }

            // Receiving OP-3 audio
            class RecThread3: Runnable{
                override fun run(){
                    SpatialAudio.recAudio("OP3")
                }
            }

            // Receiving OP-4 audio
            class RecThread4: Runnable {
                override fun run() {
                    SpatialAudio.recAudio("OP4")
                }
            }

            // Receiving OP-5 audio
            class RecThread5: Runnable {
                override fun run() {
                    SpatialAudio.recAudio("OP5")
                }
            }

            // Receiving OP-6 audio
            class RecThread6: Runnable {
                override fun run() {
                    SpatialAudio.recAudio("OP6")
                }
            }

            // Receiving OP-7 audio
            class RecThread7: Runnable {
                override fun run() {
                    SpatialAudio.recAudio("OP7")
                }
            }

            // Receiving OP-8 audio
            class RecThread8: Runnable {
                override fun run() {
                    SpatialAudio.recAudio("OP8")
                }
            }
            //endregion

            //region RUNNING THREADS: All current running threads
            val Threads = listOf<Thread>(Thread(SendStringThread()),
                Thread(RecStringThread()),
                Thread(RecThread()),
                Thread(RecThread2()),
                Thread(RecThread3()),
                Thread(RecThread4()),
                Thread(RecThread5()),
                Thread(RecThread6()),
                Thread(RecThread7()),
                Thread(RecThread8()),
                Thread(ConnectThread()),
                Thread(ConnectRecThread()),
                Thread(PTTThread()),
                Thread(HelpThread())
            )

            SpatialAudio.startThread(Threads, null)
            //endregion
            //endregion


        } catch (e: IOException){       // I/O error
            println("[Line: ${SpatialAudio.LineNumberGetter()}] Client error: " + e.message)
            e.printStackTrace()
        } catch (e: ConcurrentModificationException){
            println("[Line: ${SpatialAudio.LineNumberGetter()}] Client error: " + e.message)
            e.printStackTrace()
        }
    }
}