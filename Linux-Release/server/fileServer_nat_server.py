import select
import socket
import ssl
import queue
from pathConvertor import *
import sys

sys.path.append('..')

master_dict = {}


# class Listener(threading.Thread):
#     def __init__(self, port):
#         threading.Thread.__init__(self)
#         self.SSLContext = None
#         self.sock = None
#         self.master_port = port
#
#     def createSocket(self, port):
#         self.SSLContext = ssl.SSLContext(ssl.PROTOCOL_TLSv1_2)
#         self.SSLContext.load_cert_chain(certfile='cacert.pem', keyfile='privkey.pem')
#         self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
#         self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
#         self.sock.bind(("0.0.0.0", port))
#         self.sock.listen(0)
#         return self.SSLContext, self.sock, port
#
#     def run(self):
#         while True:
#             print('等待连接')
#             self.waitConnect()
#             print("进行数据交换")


# a read thread, read data from remote
class FILESocketExchanger(threading.Thread):
    def __init__(self, client, master):
        threading.Thread.__init__(self)
        self.client = client
        self.master = master

    def run(self):
        pass
        req = self.client.recv(BUFSIZE)
        if req:
            self.master.sendall(req)

            while True:
                try:
                    pre_read, pre_write, err = select.select([self.master, ], [self.master, ], [], 5)
                except select.error:
                    self.master.shutdown(2)
                    self.master.close()
                    break
                if len(pre_read) > 0:
                    recv = self.master.recv(BUFSIZE)
                    if recv == b'':
                        break
                    elif recv:
                        self.client.sendall(recv)

        self.client.close()
        self.master.close()


def startWLAN():
    lst = Listener(FILE_CLIENT_PORT, FILE_MASTER_PORT, FILESocketExchanger)  # create a listen thread
    lst.start()  # then start


class file_tr(threading.Thread):
    def __init__(self):
        super().__init__()

    def run(self):
        startWLAN()


if __name__ == '__main__':
    startWLAN()
