#!/usr/bin/env python3
"""Reproducible 24 fps vector-animated conceptual Egyptian Arabic motion clips.

Each frame is DRAWN at its own interpolated rig pose using Pillow, then encoded
as actual H.264 MP4 by ffmpeg. No storyboard PNGs or photo slide shows.
Visual prototypes ONLY: not verified Egyptian Sign Language instruction.
"""
import math
import subprocess
from pathlib import Path
from PIL import Image, ImageDraw

W,H,FPS,DURATION=720,480,24,3.6
OUT=Path("app/src/main/res/raw")
# Key poses: [left elbow, left hand, right elbow, right hand], absolute scene coords.
NEUTRAL=((278,288),(280,342),(443,288),(443,342))
CHEST=((278,285),(280,341),(389,245),(358,261))
OPEN=((257,247),(202,238),(463,247),(518,238))
HIGH=((250,240),(205,200),(468,240),(519,200))
RIGHT=((278,288),(280,342),(447,248),(528,250))
MOUTH=((278,288),(280,342),(400,208),(359,181))
OFFER=((270,260),(214,258),(455,248),(518,258))
THUMB=((278,288),(280,342),(438,230),(483,200))
CUP=((278,285),(280,339),(395,219),(360,185))
FORWARD=((275,258),(223,256),(455,240),(526,225))
# Different key-pose sequences correspond to concept/storyboard motion.
PHRASES={
 "ezayak":("إزيك؟","How are you?",[(0.00,CHEST),(0.20,CHEST),(0.42,OPEN),(0.65,HIGH),(0.83,OPEN),(1.00,CHEST)],"open"),
 "tamam":("تمام","Fine / OK",[(0.00,CHEST),(0.23,CHEST),(0.47,THUMB),(0.71,THUMB),(0.87,THUMB),(1.00,CHEST)],"thumb"),
 "ayez":("عايز","I want",[(0.00,CHEST),(0.23,CHEST),(0.49,OFFER),(0.72,FORWARD),(0.86,OFFER),(1.00,CHEST)],"open"),
 "shokran":("شكراً","Thank you",[(0.00,MOUTH),(0.21,MOUTH),(0.48,OFFER),(0.70,RIGHT),(0.84,CHEST),(1.00,MOUTH)],"open"),
 "mayya":("مياه","Water",[(0.00,CUP),(0.20,CUP),(0.45,MOUTH),(0.65,MOUTH),(0.85,RIGHT),(1.00,CUP)],"cup"),
 "yalla":("يلا","Let's go",[(0.00,CHEST),(0.22,CHEST),(0.42,OPEN),(0.62,FORWARD),(0.81,RIGHT),(1.00,CHEST)],"open"),
}
BLACK=(17,23,31);ORANGE=(255,107,34);WHITE=(250,252,253);BORDER=(218,227,234)
def ease(a):
 return a*a*(3-2*a)
def lerp(a,b,x):
 return (a[0]+(b[0]-a[0])*x,a[1]+(b[1]-a[1])*x)
def pose_at(keys,t):
 t=t%1
 for idx in range(len(keys)-1):
  a,pa=keys[idx]; b,pb=keys[idx+1]
  if a<=t<=b:
   f=ease((t-a)/(b-a))
   return tuple(lerp(pa[i],pb[i],f) for i in range(4))
 return keys[0][1]
def circle(d,p,r,color):
 d.ellipse((int(p[0]-r),int(p[1]-r),int(p[0]+r),int(p[1]+r)),fill=color)
def connector(d,a,b,width,fill):
 d.line([tuple(map(int,a)),tuple(map(int,b))],fill=fill,width=width,joint="curve")
 circle(d,a,width//2,fill);circle(d,b,width//2,fill)
def motion_arrow(d,a,b):
 dx=b[0]-a[0];dy=b[1]-a[1]
 length=math.hypot(dx,dy)
 if length<4:return
 # Displacement-based actual motion trail, never a sequence of still images.
 scale=1/length
 x,y=b[0],b[1]; ex=x-dx*.75;ey=y-dy*.75
 d.line((int(ex),int(ey),int(x),int(y)),fill=ORANGE,width=5)
 ux,uy=dx*scale,dy*scale
 left=(x-ux*14-uy*7,y-uy*14+ux*7)
 right=(x-ux*14+uy*7,y-uy*14-ux*7)
 d.polygon([tuple(map(int,(x,y))),tuple(map(int,left)),tuple(map(int,right))],fill=ORANGE)
def draw_hand(d,point,kind):
 x,y=point
 circle(d,(x,y),12,BLACK)
 if kind=="thumb":
  connector(d,(x+3,y-2),(x+7,y-18),5,BLACK)
 elif kind=="open":
  for dx in (-8,-3,3,8):
   connector(d,(x+dx*.5,y),(x+dx,y-12),3,BLACK)
 elif kind=="cup":
  d.arc((int(x-10),int(y-10),int(x+10),int(y+10)),0,175,fill=WHITE,width=2)
def frame(slug,t):
 key=PHRASES[slug][2];kind=PHRASES[slug][3]
 pose=pose_at(key,t)
 im=Image.new("RGB",(W,H),WHITE);d=ImageDraw.Draw(im)
 d.rounded_rectangle((22,20,W-22,H-20),radius=28,outline=BORDER,width=3)
 # Floor, display guide: keeps all films centered and legible.
 d.line((84,411,W-84,411),fill=(236,239,243),width=2)
 circle(d,(360,142),49,BLACK)
 d.rounded_rectangle((307,206,413,359),radius=42,fill=BLACK)
 connector(d,(328,345),(318,397),19,BLACK)
 connector(d,(391,345),(403,397),19,BLACK)
 for shoulder,elbow,hand in (((306,229),pose[0],pose[1]),((414,229),pose[2],pose[3])):
  connector(d,shoulder,elbow,20,BLACK);connector(d,elbow,hand,17,BLACK)
  draw_hand(d,hand,kind)
 # True frame-dependent trails of moving wrists. Static images are not swapped.
 prev=pose_at(key,max(0,t-0.048))
 for old,new in ((prev[1],pose[1]),(prev[3],pose[3])):
  motion_arrow(d,(old[0],old[1]-26),(new[0],new[1]-26))
 # Timeline sweep line and current-position dot.
 d.rounded_rectangle((100,436,620,444),radius=4,fill=(229,234,239))
 d.rounded_rectangle((100,436,100+int(520*t),444),radius=4,fill=ORANGE)
 return im
def generate(slug):
 OUT.mkdir(parents=True,exist_ok=True);dest=OUT/("motion_"+slug+".mp4")
 args=["ffmpeg","-hide_banner","-loglevel","error","-y","-f","rawvideo","-pix_fmt","rgb24","-s",f"{W}x{H}","-r",str(FPS),"-i","-",
       "-an","-c:v","libx264","-preset","veryfast","-crf","25","-pix_fmt","yuv420p","-movflags","+faststart",str(dest)]
 proc=subprocess.Popen(args,stdin=subprocess.PIPE,stderr=subprocess.PIPE)
 try:
  for i in range(round(FPS*DURATION)):
   proc.stdin.write(frame(slug,i/round(FPS*DURATION)).tobytes())
  proc.stdin.close()
  errors=proc.stderr.read();code=proc.wait()
  if code:raise RuntimeError("ffmpeg failed for "+slug+": "+errors.decode(errors="replace"))
 except Exception:
  proc.kill();proc.wait();raise
 probe=subprocess.run(["ffprobe","-v","error","-select_streams","v:0","-count_frames","-show_entries","stream=codec_name,width,height,r_frame_rate,nb_read_frames","-of","default=noprint_wrappers=1",str(dest)],capture_output=True,text=True,check=True).stdout
 assert "codec_name=h264" in probe and "width=720" in probe and "height=480" in probe and "r_frame_rate=24/1" in probe
 assert int(next(line.split("=")[1] for line in probe.splitlines() if line.startswith("nb_read_frames=")))>=80
 assert dest.stat().st_size>16000,(slug,dest.stat().st_size)
 print("VIDEO VERIFIED",slug,"bytes",dest.stat().st_size,probe.replace("\n"," "),flush=True)
def main():
 assert set(PHRASES)=={"ezayak","tamam","ayez","shokran","mayya","yalla"}
 for slug in PHRASES:
  a=frame(slug,.06).tobytes();b=frame(slug,.49).tobytes()
  assert a!=b,"No motion in "+slug
  generate(slug)
 print("PASS: six independent H.264 vector-motion MP4 videos, moving actual geometry, 24 fps",flush=True)
if __name__=="__main__":main()
