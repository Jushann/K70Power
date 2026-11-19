#!/system/bin/sh
n=/sys/class/qcom-battery/night_charging
b=/sys/class/power_supply/battery
u=/sys/class/power_supply/usb
owned=0
old=0
applied=1
phase=charge
previous=
. "$(dirname "$0")/policy.sh" || exit 1
cleanup() {
 if [ "$owned" = 1 ]; then
  if [ "$(cat "$n")" = "$applied" ]; then
   echo "$old" > "$n" || return 1
   [ "$(cat "$n")" = "$old" ] || return 1
  fi
  owned=0
 fi
}
trap cleanup EXIT HUP INT TERM
# Device families using the same binary Qualcomm charging control.
device=$(getprop ro.product.device)
case "$device" in
 manet|socrates|peridot) ;;
 myron)
  n=
  for dir in /sys/devices/platform/soc/soc:smart_charge /sys/devices/platform/smart_charge; do
   if [ -f "$dir/soc_limit" ] && [ -r "$dir/smart_night" ]; then
    n=$dir/soc_limit
    break
   fi
  done
  ;;
 *) echo "当前机型尚未适配：$device"; exit 1 ;;
esac
if [ ! -r "$n" ] || [ ! -w "$n" ]; then
 echo "当前内核未提供可读写的充电控制接口：$device"
 exit 1
fi
reading=$(cat "$n" 2>/dev/null)
case "$device:$reading" in
 myron:[1-9]|myron:[1-9][0-9]|myron:100) ;;
 myron:*) echo "无法识别 soc_limit 状态：$reading"; exit 1 ;;
 *:0|*:1) ;;
 *) echo "无法识别充电接口状态：$device"; exit 1 ;;
esac
for sensor in "$b/capacity" "$b/temp" "$u/online"; do
 if [ ! -r "$sensor" ]; then
  echo "当前内核缺少控制所需的读数：$sensor"
  exit 1
 fi
done
cpu_zones=
for zone in /sys/class/thermal/thermal_zone*; do
 kind=$(cat "$zone/type" 2>/dev/null)
 case "$kind" in cpu-[0-9]*|cpuss-[0-9]*) cpu_zones="$cpu_zones $zone";; esac
done
while read -r -t 15 mode low high; do
 [ "$mode" = stop ] && break
 online=$(cat "$u/online")
 capacity=$(cat "$b/capacity")
 temp=$(cat "$b/temp")
 if [ "$device" = myron ] && [ "$mode:$low:$high" != "$previous" ]; then
  cleanup || { echo "恢复充电上限失败"; exit 1; }
 fi
 decide || { echo 'ERROR: invalid settings or telemetry'; break; }
 if [ "$desired" = 1 ]; then
  if [ "$owned" = 0 ]; then
   old=$(cat "$n")
   if [ "$device" = myron ]; then
    [ "$old" = 100 ] || { echo '已有系统充电上限，已停止控制'; exit 1; }
    applied=1
    [ "$mode" = threshold ] && applied=$low
   else
    [ "$old" = 0 ] || { echo '充电接口已由其他控制器使用'; exit 1; }
   fi
  fi
  if [ "$device" = myron ] && [ "$(cat "$dir/smart_night")" != 0 ]; then
   echo '系统夜间充电已接管，已停止控制'
   exit 1
  fi
  if [ "$owned" = 0 ]; then
   owned=1
   echo "$applied" > "$n" || { echo '写入充电接口失败'; exit 1; }
  fi
  [ "$(cat "$n")" = "$applied" ] || { echo '系统已更改充电接口，已停止控制'; exit 1; }
 else
  cleanup || { echo 'ERROR: restore failed'; break; }
 fi
 cpu_max=
 cpu_source=
 for zone in $cpu_zones; do
  value=$(cat "$zone/temp" 2>/dev/null)
  case "$value" in ''|*[!0-9]*) continue;; esac
  [ "$value" -ge 1000 ] && [ "$value" -le 150000 ] || continue
  if [ -z "$cpu_max" ] || [ "$value" -gt "$cpu_max" ]; then
   cpu_max=$value
   cpu_source=$(cat "$zone/type" 2>/dev/null)
  fi
 done
 state=$owned
 [ "$device" = myron ] || state=$(cat "$n")
 echo "DATA|$mode|$state|$online|$(cat "$b/voltage_now" 2>/dev/null)|$(cat "$b/current_now" 2>/dev/null)|$(cat "$u/voltage_now" 2>/dev/null)|$(cat "$u/current_now" 2>/dev/null)|$capacity|$temp|$(cat "$b/status")|$cpu_max|$cpu_source"
done
cleanup && echo RESTORED
