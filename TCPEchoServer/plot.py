import matplotlib.pyplot as plt
import numpy as np
import os

path = "./stats"

i = 0

for file in os.listdir(path):
    X, Y = np.loadtxt(path + "/" + file, delimiter=',', unpack=True)
    
    plt.scatter(X, Y)
    plt.title('Line Graph using NUMPY')
    plt.xlabel('X')
    plt.ylabel('Y')
    plt.savefig(os.path.join('graficos', f'neighborhood_{i}.png'))
    