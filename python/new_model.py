from ann import *
import sys


if __name__ == "__main__":

    ann = Ann(batch_size=30, epochs=30, lr=0.0001, l2=0.001)
    ann.main()
    ann.save_model(sys.argv[1])
    traced_cell = torch.jit.trace(ann.model, torch.rand(1,3,160,160).to(ann.device))
    traced_cell.save(sys.argv[1])
